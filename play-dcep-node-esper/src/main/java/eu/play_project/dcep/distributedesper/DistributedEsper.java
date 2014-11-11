package eu.play_project.dcep.distributedesper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.event_processing.events.types.Event;
import org.openrdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.SimplePublishApi;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedesper.configurations.Config4store;
import eu.play_project.dcep.distributedesper.configurations.ConfigLocal;
import eu.play_project.dcep.distributedesper.configurations.ConfigVirtuoso;
import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.dcep.node.api.DcepNodeConfiguringApi;
import eu.play_project.dcep.node.api.DcepNodeException;
import eu.play_project.dcep.node.api.EcConnectionManager;
import eu.play_project.dcep.node.api.EcConnectionmanagerException;
import eu.play_project.platformservices.querydispatcher.query.eventImpl.rdf.sesame.SesameEventModel;
import eu.play_project.platformservices.querydispatcher.query.eventImpl.rdf.sesame.SesameMapEvent;
import eu.play_project.platformservices.querydispatcher.query.simulation.TestStmtListener;
import eu.play_project.play_commons.eventtypes.EventTypeMetadata;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetails;

public class DistributedEsper implements DcepManagmentApi, DcepNodeApi<Event>, DcepNodeConfiguringApi<Event> {

	private final Logger logger = LoggerFactory.getLogger(DistributedEsper.class);
	private final Map<String, BdplQuery> registeredQueries = Collections.synchronizedMap(new HashMap<String, BdplQuery>());
	private EcConnectionManager<Event> ecConnectionManager;
	private final EPServiceProvider epService;
	private final EPRuntime epRuntime;
	private boolean init;
	private final Set<SimplePublishApi<Event>> eventSinks = Collections
			.synchronizedSet(new HashSet<SimplePublishApi<Event>>());

	public DistributedEsper() {
		logger.info("Initialising {} component.", this.getClass().getSimpleName());

		this.epService = EPServiceProviderManager.getDefaultProvider();
		this.epRuntime = epService.getEPRuntime();

		this.init = true;
	}

	
	@Override
	public void finalize() {
		logger.info("Terminating {} component.", this.getClass()
				.getSimpleName());
		if (init) {
			if(ecConnectionManager!=null) this.ecConnectionManager.destroy();
			this.eventSinks.clear();
			this.init = false;
		}
	}

	
	@Override
	public void attach(SimplePublishApi<Event> subscriber) {
		logger.debug("New subscriber.");
		this.eventSinks.add(subscriber);
	}

	@Override
	public void detach(SimplePublishApi<Event> subscriber) {
		this.eventSinks.remove(subscriber);
	}

	@Override
	public void publish(Event event) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}
		
		try {
			// TODO stuehmer: create model with RDF2Go
			Model m = (Model)event.getModel().getUnderlyingModelImplementation();
			this.epRuntime.sendEvent(new SesameMapEvent(new SesameEventModel(m)), EventTypeMetadata.getType(event));
				//System.out.println("Send Event1");
		} catch (EPException e) {
			logger.warn("Cannot publish event.");		}
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
		logger.debug("ELE: {}", bdplQuery.getTargetQuery());

		if(this.registeredQueries.containsKey(bdplQuery.getDetails().getQueryId())) {
			String error = "Pattern ID already exists: " + bdplQuery.getDetails().getQueryId();
			logger.error(error);
			throw new DcepManagementException(error);
		}
		
		QueryDetails qd = (QueryDetails)bdplQuery.getDetails();
		try {
			
			this.registeredQueries.put(qd.getQueryId(), bdplQuery);
		
			logger.debug("Register query: {}", bdplQuery.getTargetQuery());
			
			EPStatement testStmt = epService.getEPAdministrator().createEPL(bdplQuery.getTargetQuery(), bdplQuery.getDetails().getQueryId());
			testStmt.addListener(new TestStmtListener());
					
			// Configure ETALIS to inform output listener if complex event of new type appeared.
			// FIXME stuehmer
			
			// Make subscriptions.
			this.ecConnectionManager.registerEventPattern(bdplQuery);
		} catch (EcConnectionmanagerException e) {
			this.unregisterEventPattern(qd.getQueryId());
			throw new DcepManagementException(e.getMessage());
		} catch (Exception e) {
			this.unregisterEventPattern(qd.getQueryId());
			throw new DcepManagementException(e.getMessage());
		}

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
			epService.getEPAdministrator().getStatement(queryId).destroy();

			this.ecConnectionManager.unregisterEventPattern(registeredQueries
					.get(queryId));
			this.registeredQueries.remove(queryId);
		}
		else {
			logger.warn("Event pattern to be removed was not found at 'DistributedEsper' Rule ID = "
					+ queryId);
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
	public void setEcConnectionManager(EcConnectionManager<Event> ecConnectionManager) {
		this.ecConnectionManager = ecConnectionManager;
	}


	@Override
	public EcConnectionManager<Event> getEcConnectionManager() {
		return this.ecConnectionManager;
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
					new ConfigLocal(historicDataFileNames).configure(this);
				}
				else if (middleware.equals("virtuoso")) {
					new ConfigVirtuoso().configure(this);
				}
				else if (middleware.equals("4store")) {
					new Config4store().configure(this);
				}
				else {
					throw new DcepManagementException(String.format(
							"Specified middleware is not implemented: %s.", middleware));
				}
				init = true;
			} catch (DcepNodeException e) {
				throw new DcepManagementException(e.getMessage());
			}
		} else {
			logger.warn("DistributedEtalis is already configured");
		}
	}
}
