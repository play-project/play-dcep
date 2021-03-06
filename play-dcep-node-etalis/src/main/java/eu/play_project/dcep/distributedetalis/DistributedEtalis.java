package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jpl.PrologException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.api.ConfigApi;
import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.api.DcepTestApi;
import eu.play_project.dcep.api.SimplePublishApi;
import eu.play_project.dcep.api.measurement.MeasurementConfig;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.configurations.DetailsConfigLocalJena;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfig4store;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigNet;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigVirtuoso;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import eu.play_project.play_platformservices.api.BdplQuery;
import fr.inria.eventcloud.api.CompoundEvent;

/**
 * Distributed Etalis component. This component is a standalone event processing
 * agent which can be instantiated multiple times to create a distributed
 * network.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 */
public class DistributedEtalis implements DcepMonitoringApi, DcepManagmentApi,
		DcepTestApi, ComponentInitActive, ComponentEndActive,
		ConfigApi, DEtalisConfigApi, Serializable {

	private static final long serialVersionUID = 100L;
	private int prologTriggerDelay = 100; // Delay between two prolog trigger calls.
	private String name;
	private JtalisContextImpl etalis; // ETALIS Object
	private JtalisOutputProvider eventOutputProvider;
	private JtalisInputProvider eventInputProvider;
	private final Logger logger = LoggerFactory.getLogger(DistributedEtalis.class);
	private Map<String, BdplQuery> registeredQueries = Collections.synchronizedMap(new HashMap<String, BdplQuery>());
	private EcConnectionManager ecConnectionManager;
	private MeasurementUnit measurementUnit;
	private PrologSemWebLib semWebLib;
	private boolean init = false;
	private final Set<SimplePublishApi> eventSinks = Collections
			.synchronizedSet(new HashSet<SimplePublishApi>());

	Service service;

	{
		/*
		 * Set up logging for jtalis (using JUL -> slf4j)
		 */
		
		// Optionally remove existing handlers attached to j.u.l root logger
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		
		// add SLF4JBridgeHandler to j.u.l's root logger, should be done once
		// during the initialization phase of your application
		SLF4JBridgeHandler.install();
	}
	
	// Only for ProActive
	public DistributedEtalis() {
	}

	// Only for local use (testing)
	public DistributedEtalis(String name) {
		this.name = name;
		this.initComponentActivity(null);
	}

	@Override
	public void initComponentActivity(Body body) {
		logger.info("Initialising {} component.", this.getClass().getSimpleName());
	}

	@Override
	public void endComponentActivity(Body arg0) {
		logger.info("Terminating {} component.", this.getClass()
				.getSimpleName());
		if (init) {
			if(ecConnectionManager!=null) this.ecConnectionManager.destroy();
			this.etalis.shutdown();
			this.eventSinks.clear();
			this.init = false;
		}
	}

	@Override
	public void registerEventPattern(BdplQuery bdplQuery) throws DcepManagementException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()+ " has not been initialized.");
		}
		if (bdplQuery.getDetails() == null) {
			throw new IllegalArgumentException("QueryDetails is not set");
		}
		logger.info("New event pattern is being registered at {} with queryId = {}",
				this.getClass().getSimpleName(), bdplQuery
				.getDetails().getQueryId());
		logger.debug("ELE: {}", bdplQuery.getEleQuery());

		if(this.registeredQueries.containsKey(bdplQuery.getDetails().getQueryId())) {
			String error = "Pattern ID already exists: " + bdplQuery.getDetails().getQueryId();
			logger.error(error);
			throw new DcepManagementException(error);
		}
		
		try {
			this.registeredQueries.put(bdplQuery.getDetails().getQueryId(), bdplQuery);
		
			logger.debug("Register query: {}", bdplQuery.getEleQuery());
			
			etalis.addDynamicRuleWithId(quoteForProlog(bdplQuery.getDetails().getQueryId()) + bdplQuery.getDetails().getEtalisProperty(), bdplQuery.getEleQuery());
			// Start tumbling window. (If a tumbling window was defined.)
			if (!etalis.getEngineWrapper().executeGoal(bdplQuery.getDetails().getTumblingWindow())) {
				throw new DistributedEtalisException("Error registering tumbling window for queryId " + bdplQuery.getDetails().getQueryId());
			}
			
			//Register db queries.
			for (String dbQuery : bdplQuery.getDetails().getRdfDbQueries()) {
				if (!etalis.getEngineWrapper().executeGoal("assert(" + dbQuery + ")")) {
					throw new DistributedEtalisException("Error registering RdfDbQueries for queryId " + bdplQuery.getDetails().getQueryId());
				}
			}

			for (String triggerPattern : bdplQuery.getDetails().getTriggerPattern()) {
				System.out.println("Register virual event pattern: " + triggerPattern );

				logger.debug("Register virual event pattern: {}", triggerPattern );
				etalis.addDynamicRuleWithId(quoteForProlog(bdplQuery.getDetails().getQueryId()), triggerPattern);
			}
			
			// Configure ETALIS to inform output listener if complex event of new type appeared.
			etalis.addEventTrigger(bdplQuery.getDetails().getComplexType() + "/_");
		
			// Make subscriptions.
			this.ecConnectionManager.registerEventPattern(bdplQuery);
		} catch (PrologException e) {
			e.printStackTrace();
			this.unregisterEventPattern(bdplQuery.getDetails().getQueryId());
			throw new DcepManagementException(e.getMessage());
		} catch (EcConnectionmanagerException e) {
			e.printStackTrace();
			this.unregisterEventPattern(bdplQuery.getDetails().getQueryId());
			throw new DcepManagementException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			this.unregisterEventPattern(bdplQuery.getDetails().getQueryId());
			throw new DcepManagementException(e.getMessage());
		}
	}

	@Override
	public BdplQuery getRegisteredEventPattern(String queryId) throws DcepManagementException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}
		
		if (this.registeredQueries.get(queryId) != null) {
			return this.registeredQueries.get(queryId);
		}
		else {
			throw new DcepManagementException("No event pattern is registered with id: " + queryId);
		}
	}

	@Override
	public Map<String, BdplQuery> getRegisteredEventPatterns() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return this.registeredQueries;
	}

	@Override
	public void unregisterEventPattern(String queryId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		if (this.registeredQueries.containsKey(queryId)) {
			logger.info("Removing event pattern at 'DistributedEtalis' Rule ID = "
					+ queryId);
			try {
				etalis.removeDynamicRule(queryId);
			} catch (PrologException e) {
				logger.warn(String.format("Problem removing event pattern '%s': %s: %s", queryId, e.getClass().getSimpleName(), e.getMessage()));
			}
			this.ecConnectionManager.unregisterEventPattern(registeredQueries
					.get(queryId));
			this.registeredQueries.remove(queryId);
		}
		else {
			logger.warn("Event pattern to be removed was not found at 'DistributedEtalis' Rule ID = "
					+ queryId);
		}
	}

	@Override
	public NodeMeasurementResult getMeasuredData(String queryId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return measurementUnit.getMeasuringResults();
	}

	@Override
	public void setConfig(String middleware) throws DcepManagementException {
		if(init != true) {
			try {
				if(middleware.equals("local")) {
					//Read historic data filenames.
					List<String> historicDataFileNames = new ArrayList<String>();
					for (String historicDataFileName : DcepConstants.getProperties().getProperty("dcep.local.historicdata.source", "historical-data/play-bdpl-telco-recom-tweets-historic-data.trig").split(",")) {
						historicDataFileNames.add(historicDataFileName.trim());
					}
					new DetalisConfigLocal(historicDataFileNames).configure(this);
				}
				else if(middleware.equals("local.jean")) {
					//Read historic data filenames.
					List<String> historicDataFileNames = new ArrayList<String>();
					for (String historicDataFileName : DcepConstants.getProperties().getProperty("dcep.local.historicdata.source", "historical-data/play-bdpl-telco-recom-tweets-historic-data.trig").split(",")) {
						historicDataFileNames.add(historicDataFileName.trim());
					}
					new DetailsConfigLocalJena(historicDataFileNames).configure(this);
				}
				else if (middleware.equals("eventcloud")) {
					new DetalisConfigNet().configure(this);
				}
				else if (middleware.equals("virtuoso")) {
					new DetalisConfigVirtuoso().configure(this);
				}
				else if (middleware.equals("4store")) {
					new DetalisConfig4store().configure(this);
				}
				else {
					throw new DcepManagementException(String.format(
							"Specified middleware is not implemented: %s.", middleware));
				}
				
				// Start prolog triger thread.
				new PrologTriggerThread(etalis, prologTriggerDelay);
				
				init = true;
			} catch (DistributedEtalisException e) {
				throw new DcepManagementException(e.getMessage());
			}
		} else {
			logger.warn("DistributedEtalis is already configured");
		}
	}

	@Override
	public void setConfigLocal(String rdf) throws DcepManagementException {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	@Override
	public JtalisInputProvider getEventInputProvider() {
		return eventInputProvider;
	}

	@Override
	public void publish(CompoundEvent event) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}
		
		eventInputProvider.notify(event);
	}

	@Override
	public void attach(SimplePublishApi subscriber) {
		logger.debug("New subscriber.");
		this.eventSinks.add(subscriber);
	}

	@Override
	public void detach(SimplePublishApi subscriber) {
		this.eventSinks.remove(subscriber);
	}

	@Override
	public void setEcConnectionManager(EcConnectionManager ecConnectionManager) {
		this.ecConnectionManager = ecConnectionManager;
	}

	@Override
	public void setEventOutputProvider(JtalisOutputProvider eventOutputProvider) {
		this.eventOutputProvider = eventOutputProvider;
	}

	@Override
	public void setEventInputProvider(JtalisInputProvider eventInputProvider) {
		this.eventInputProvider = eventInputProvider;
	}

	@Override
	public void setSemWebLib(PrologSemWebLib semWebLib) {
		this.semWebLib = semWebLib;
	}

	@Override
	public void setEtalis(JtalisContextImpl etalis) {
		this.etalis = etalis;
	}

	@Override
	public DistributedEtalis getDistributedEtalis(){
		return this;
	}

	@Override
	public Map<String, BdplQuery> getRegisteredQueries() {
		return registeredQueries;
	}

	@Override
	public void setRegisteredQueries(Map<String, BdplQuery> registeredQueries) {
		this.registeredQueries = registeredQueries;
	}

	@Override
	public EcConnectionManager getEcConnectionManager() {
		return ecConnectionManager;
	}

	@Override
	public Set<SimplePublishApi> getEventSinks() {
		return eventSinks;
	}

	@Override
	public JtalisContextImpl getEtalis() {
		return etalis;
	}

	@Override
	public JtalisOutputProvider getEventOutputProvider() {
		return eventOutputProvider;
	}

	@Override
	public void measurePerformance(MeasurementConfig config) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		measurementUnit.startMeasurement(config.getMeasurementPeriod());
	}
	
	@Override
	public void setMeasurementUnit(MeasurementUnit measurementUnit) {
		this.measurementUnit = measurementUnit;
	}
}