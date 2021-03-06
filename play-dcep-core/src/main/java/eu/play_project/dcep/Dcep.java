package eu.play_project.dcep;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.ConfigApi;
import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.api.DcepTestApi;
import eu.play_project.dcep.api.SimplePublishApi;
import eu.play_project.dcep.api.measurement.MeasurementConfig;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.play_platformservices.api.BdplQuery;
import fr.inria.eventcloud.api.CompoundEvent;

/**
 * Distributed Complex Event Processing (DCEP) component. This component is a
 * container to hold one or more instances of {@linkplain DistributedEtalis} to
 * create a distributed network.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 */
public class Dcep implements DcepMonitoringApi, DcepManagmentApi,
ComponentInitActive, ComponentEndActive, DcepTestApi,
Serializable {

	private static final long serialVersionUID = 100L;
	private DcepTestApi dEtalisTest;
	private DcepMonitoringApi dEtalisMonitoring;
	private DcepManager dcepManager;
	private ConfigApi configApi;
	private Component dEtalis;
	private Logger logger;
	private boolean init = false; // Shows if variables for other components are
	// initialized.

	public Dcep() {
	}

	@Override
	public void initComponentActivity(Body body) {
		logger = LoggerFactory.getLogger(this.getClass());
		logger.info("Initialising {} component.", this.getClass()
				.getSimpleName());
	}

	@Override
	public void endComponentActivity(Body arg0) {
		logger.info("Terminating {} component.", this.getClass()
				.getSimpleName());
		try {
			GCM.getGCMLifeCycleController(this.dEtalis).stopFc();
			GCM.getGCMLifeCycleController(this.dEtalis).terminateGCMComponent();
		} catch (IllegalLifeCycleException e) {
			logger.error("Error terminating subcomponent.", e);
		} catch (NullPointerException e) {
			logger.error("Error terminating subcomponent.", e);
		} catch (NoSuchInterfaceException e) {
			logger.error("Error terminating subcomponent.", e);
		}
	}

	@Override
	public void registerEventPattern(BdplQuery bdplQuery) throws DcepManagementException {

		logger.debug("Pattern reached DCEP facade: "
				+ bdplQuery.getEleQuery());

		if(!init) init();
		dcepManager.getManagementApi().registerEventPattern(bdplQuery);
	}

	@Override
	public BdplQuery getRegisteredEventPattern(String queryId) throws DcepManagementException{
		if(!init) init();
		return dcepManager.getManagementApi().getRegisteredEventPattern(queryId);
	}

	@Override
	public Map<String, BdplQuery> getRegisteredEventPatterns() {
		if(!init) init();
		return dcepManager.getManagementApi().getRegisteredEventPatterns();
	}

	@Override
	public void unregisterEventPattern(String queryID) {
		if(!init) init();
		dcepManager.getManagementApi().unregisterEventPattern(queryID);
	}

	@Override
	public void publish(CompoundEvent event) {
		if(!init) init();
		dEtalisTest.publish(event);
	}

	@Override
	public void attach(SimplePublishApi subscriber) {
		if(!init) init();
		dEtalisTest.attach(subscriber);
	}

	@Override
	public void detach(SimplePublishApi subscriber) {
		if(!init) init();
		dEtalisTest.detach(subscriber);
	}

	/**
	 * Init connections to dEtalis components.
	 */
	private boolean init() {

		if (init) {
			logger.warn("{} has already been initialized. Skipping.", this.getClass().getSimpleName());
		}
		else {
			Factory factory;

			try {
				factory = FactoryFactory.getFactory();

				HashMap<String, Object> context = new HashMap<String, Object>();

				this.dEtalis = (Component) factory.newComponent(
						"DistributedEtalis", context);
				GCM.getGCMLifeCycleController(dEtalis).startFc();

				dEtalisTest = ((DcepTestApi) dEtalis
						.getFcInterface(DcepTestApi.class.getSimpleName()));
				dEtalisMonitoring = ((DcepMonitoringApi) dEtalis
						.getFcInterface(DcepMonitoringApi.class.getSimpleName()));
				configApi = ((ConfigApi)dEtalis.getFcInterface(ConfigApi.class.getSimpleName()));
				configNodeInstance(configApi);
				
				// Register apis
				Fractive.registerByName(this.dEtalis, "dEtalis");

			} catch (NoSuchInterfaceException e) {
				logger.error("Error initialising DCEP: {}", e.getMessage());
				logger.debug("Error initialising DCEP:", e);
				//throw new DcepException("Error initialising DCEP: ", e);
			} catch (ADLException e) {
				logger.error("Error initialising DCEP: {}", e.getMessage());
				logger.debug("Error initialising DCEP:", e);
				//throw new DcepException("Error initialising DCEP: ", e);
			} catch (IllegalLifeCycleException e) {
				logger.error("Error initialising DCEP: {}", e.getMessage());
				logger.debug("Error initialising DCEP:", e);
				//throw new DcepException("Error initialising DCEP: ", e);
			} catch (ProActiveException e) {
				logger.error("Error initialising DCEP: {}", e.getMessage());
				logger.debug("Error initialising DCEP:", e);
				//throw new DcepException("Error initialising DCEP: ", e);
			} catch (DcepManagementException e) {
				logger.error("Error initialising DCEP: {}", e.getMessage());
				logger.debug("Error initialising DCEP:", e);
			}
			
			dcepManager = new DcepManager();
			dcepManager.init();
			init = true;
		}
		return init;
	}

	public void configNodeInstance(ConfigApi configApi) throws DcepManagementException {
		// get property or set default:
		String middleware = DcepConstants.getProperties().getProperty("dcep.middleware", "local");

		configApi.setConfig(middleware);
	}

	@Override
	public void measurePerformance(MeasurementConfig config) {
		if(!init) init();
		dEtalisMonitoring.measurePerformance(config);
	}

	@Override
	public NodeMeasurementResult getMeasuredData(String queryId) {
		if(!init) init();
		return dEtalisMonitoring.getMeasuredData(queryId);
	}
}
