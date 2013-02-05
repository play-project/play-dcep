<<<<<<< HEAD
package eu.play_project.dcep.distributedetalis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.Configuration;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
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
		DistributedEtalisTestApi, ComponentInitActive, ComponentEndActive,
		ConfigApi, DEtalisConfigApi {

	private String name;
	private JtalisContextImpl etalis; // ETALIS Object
	private JtalisOutputProvider eventOutputProvider;
	private JtalisInputProvider eventInputProvider;
	private PrologSemWebLib semWebLib;
	private Logger logger;
	private Map<String, EpSparqlQuery> registeredQuerys = Collections
			.synchronizedMap(new HashMap<String, EpSparqlQuery>());
	private EcConnectionManager ecConnectionManager;
	private MeasurementUnit measurementUnit;
	private boolean init = false;
	private Set<SimplePublishApi> eventSinks = Collections
			.synchronizedSet(new HashSet<SimplePublishApi>());

	Service service;

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

		logger = LoggerFactory.getLogger(DistributedEtalis.class);
		logger.info("Initialising {} component.", this.getClass().getSimpleName());
	}

	@Override
	public void endComponentActivity(Body arg0) {
		logger.info("Terminating {} component.", this.getClass()
				.getSimpleName());
		if (init) {
			this.etalis.shutdown();
			if(ecConnectionManager!=null) this.ecConnectionManager.destroy();
			this.eventSinks.clear();
			this.init = false;
		}
	}

	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}
		if (epSparqlQuery.getQueryDetails() == null) {
			throw new IllegalArgumentException("QueryDetails is not set");
		}
		logger.info("New event pattern registered at {} with queryId = {}",
				this.getClass().getSimpleName(), epSparqlQuery
						.getQueryDetails().getQueryId());
		logger.debug("ELE: " + epSparqlQuery.getEleQuery());

		this.registeredQuerys.put(epSparqlQuery.getQueryDetails().getQueryId(),
				epSparqlQuery);

		// Deal with sliding time windows:
		String windowDefinition = "";
		if (!epSparqlQuery.getQueryDetails().getWindowTime().equals("")
				&& !epSparqlQuery.getQueryDetails().getWindowTime().equals("0")) {
			windowDefinition = "([property(event_rule_window, "
					+ epSparqlQuery.getQueryDetails().getWindowTime() + ")])";
			logger.info("Adding ETALIS rule with time window "
					+ windowDefinition);
		}
		logger.debug("Register query: " + epSparqlQuery.getEleQuery());
		etalis.addDynamicRuleWithId("'"
				+ epSparqlQuery.getQueryDetails().getQueryId() + "'"
				+ windowDefinition, epSparqlQuery.getEleQuery());

		this.ecConnectionManager.registerEventPattern(epSparqlQuery);
	}

	@Override
	public EpSparqlQuery getRegisteredEventPattern(String queryId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return this.registeredQuerys.get(queryId);
	}

	@Override
	public Map<String, EpSparqlQuery> getRegisteredEventPatterns() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return this.registeredQuerys;
	}

	@Override
	public void unregisterEventPattern(String queryId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		logger.info("Removing event pattern at 'DistributedEtalis' Rule ID = "
				+ queryId);
		etalis.removeDynamicRule(queryId);
		this.registeredQuerys.remove(queryId);
		this.ecConnectionManager.unregisterEventPattern(registeredQuerys
				.get(queryId));
	}

	@Override
	public NodeMeasuringResult measurePerformance(int measuringPeriod) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		// measurementUnit.startMeasurement(measuringPeriod);
		return null;
	}

	@Override
	public NodeMeasuringResult getMeasurementData() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return measurementUnit.getMeasuringResults();
	}

	@Override
	public void setConfig(Configuration configuration) {
		configuration.configure(this);
		init = true;
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

	public JtalisInputProvider getEventInputProvider() {
		return eventInputProvider;
	}

	@Override
	public void publish(CompoundEvent event) {
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

	public Logger getLogger() {
		return logger;
	}

	public Map<String, EpSparqlQuery> getRegisteredQuerys() {
		return registeredQuerys;
	}

	public void setRegisteredQuerys(Map<String, EpSparqlQuery> registeredQuerys) {
		this.registeredQuerys = registeredQuerys;
	}

	public EcConnectionManager getEcConnectionManager() {
		return ecConnectionManager;
	}

	public Set<SimplePublishApi> getEventSinks() {
		return eventSinks;
	}

	public JtalisContextImpl getEtalis() {
		return etalis;
	}

	public JtalisOutputProvider getEventOutputProvider() {
		return eventOutputProvider;
	}

}
=======
package eu.play_project.dcep.distributedetalis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.Configuration;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
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
		DistributedEtalisTestApi, ComponentInitActive, ComponentEndActive,
		ConfigApi, DEtalisConfigApi {

	private String name;
	private JtalisContextImpl etalis; // ETALIS Object
	private JtalisOutputProvider eventOutputProvider;
	private JtalisInputProvider eventInputProvider;
	private PrologSemWebLib semWebLib;
	private Logger logger;
	private Map<String, EpSparqlQuery> registeredQuerys = Collections
			.synchronizedMap(new HashMap<String, EpSparqlQuery>());
	private EcConnectionManager ecConnectionManager;
	private MeasurementUnit measurementUnit;
	private boolean init = false;
	private Set<SimplePublishApi> eventSinks = Collections
			.synchronizedSet(new HashSet<SimplePublishApi>());

	Service service;

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

		logger = LoggerFactory.getLogger(DistributedEtalis.class);
		logger.info("Initialising {} component.", this.getClass().getSimpleName());
	}

	@Override
	public void endComponentActivity(Body arg0) {
		logger.info("Terminating {} component.", this.getClass()
				.getSimpleName());
		if (init) {
			this.etalis.shutdown();
			if(ecConnectionManager!=null) this.ecConnectionManager.destroy();
			this.eventSinks.clear();
			this.init = false;
		}
	}

	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}
		if (epSparqlQuery.getQueryDetails() == null) {
			throw new IllegalArgumentException("QueryDetails is not set");
		}
		logger.info("New event pattern registered at {} with queryId = {}",
				this.getClass().getSimpleName(), epSparqlQuery
						.getQueryDetails().getQueryId());
		logger.debug("ELE: " + epSparqlQuery.getEleQuery());

		this.registeredQuerys.put(epSparqlQuery.getQueryDetails().getQueryId(),
				epSparqlQuery);

		// Deal with sliding time windows:
		String windowDefinition = "";
		if (!epSparqlQuery.getQueryDetails().getWindowTime().equals("")
				&& !epSparqlQuery.getQueryDetails().getWindowTime().equals("0")) {
			windowDefinition = "([property(event_rule_window, "
					+ epSparqlQuery.getQueryDetails().getWindowTime() + ")])";
			logger.info("Adding ETALIS rule with time window "
					+ windowDefinition);
		}
		logger.debug("Register query: " + epSparqlQuery.getEleQuery());
		etalis.addDynamicRuleWithId("'"
				+ epSparqlQuery.getQueryDetails().getQueryId() + "'"
				+ windowDefinition, epSparqlQuery.getEleQuery());

		this.ecConnectionManager.registerEventPattern(epSparqlQuery);
	}

	@Override
	public EpSparqlQuery getRegisteredEventPattern(String queryId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return this.registeredQuerys.get(queryId);
	}

	@Override
	public Map<String, EpSparqlQuery> getRegisteredEventPatterns() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return this.registeredQuerys;
	}

	@Override
	public void unregisterEventPattern(String queryId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		logger.info("Removing event pattern at 'DistributedEtalis' Rule ID = "
				+ queryId);
		etalis.removeDynamicRule(queryId);
		this.registeredQuerys.remove(queryId);
		this.ecConnectionManager.unregisterEventPattern(registeredQuerys
				.get(queryId));
	}

	@Override
	public NodeMeasuringResult measurePerformance(int measuringPeriod) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		// measurementUnit.startMeasurement(measuringPeriod);
		return null;
	}

	@Override
	public NodeMeasuringResult getMeasurementData() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		return measurementUnit.getMeasuringResults();
	}

	@Override
	public void setConfig(Configuration configuration) {
		configuration.configure(this);
		init = true;
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

	public JtalisInputProvider getEventInputProvider() {
		return eventInputProvider;
	}

	@Override
	public void publish(CompoundEvent event) {
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

	public Logger getLogger() {
		return logger;
	}

	public Map<String, EpSparqlQuery> getRegisteredQuerys() {
		return registeredQuerys;
	}

	public void setRegisteredQuerys(Map<String, EpSparqlQuery> registeredQuerys) {
		this.registeredQuerys = registeredQuerys;
	}

	public EcConnectionManager getEcConnectionManager() {
		return ecConnectionManager;
	}

	public Set<SimplePublishApi> getEventSinks() {
		return eventSinks;
	}

	public JtalisContextImpl getEtalis() {
		return etalis;
	}

	public JtalisOutputProvider getEventOutputProvider() {
		return eventOutputProvider;
	}

}
>>>>>>> 1801c9c1039ae3f26827d175e7ad869de92b7c3a
