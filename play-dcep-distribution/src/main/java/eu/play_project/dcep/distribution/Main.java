package eu.play_project.dcep.distribution;

import java.util.HashMap;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.io.IOUtils;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;


public class Main implements Daemon {

	private static final String propertiesFile = "proactive.java.policy";
	private static Logger logger;
	private Component root;

	/**
	 * Start DCEP. There are three ways to stop it: (1) by killing the main
	 * thread a shutdownhook is caught (2) by sending 3 newlines on stdin (3)
	 * when an exception happens during start.
	 */
	public static void main(String[] args) {
		final Main main = new Main();

		try {
			main.start();
			
			Runtime.getRuntime().addShutdownHook(new Thread(){
			    @Override
				public void run() {
			        logger.debug("Shutdown hook was invoked. Shutting down...");
			        try {
						main.stop();
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
			    }
			});
						
			System.out.println("Press 3x RETURN to shutdown the application");
			System.in.read();
			System.in.read();
			System.in.read();
			
			main.stop();
			
		} catch (Exception e){
			logger.error(e.getMessage(), e);
		} finally {
			try {
				main.stop();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException,
			Exception {
	}

	@Override
	public void start() throws Exception {
		logger = LoggerFactory.getLogger(Main.class);

		/*
		 * Set up Components
		 */
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(propertiesFile);
		CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");

		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		root = (Component) factory.newComponent("PlayPlatform", context);

		GCM.getGCMLifeCycleController(root).startFc();

		boolean init = false;
		
		// Wait for all subcomponents to be started
		while (!init) {
			boolean overallInitStatus = true;
			for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
				overallInitStatus = overallInitStatus && GCM.getGCMLifeCycleController(subcomponent).getFcState().equals(LifeCycleController.STARTED);
			}
			if (overallInitStatus == true) {
				init = true;
			}
			else {
				logger.info("Wait for all subcomponents to be started...");
			}
			Thread.sleep(500);
		}
				
		
		// Get interfaces
		QueryDispatchApi queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));

		/*
		 *  Compile and Deploy Queries
		 */
		for (String queryFileName : DcepConstants.getProperties().getProperty("dcep.startup.registerqueries").split(",")) {
			queryFileName = queryFileName.trim();
			try {
				String	queryString = IOUtils.toString(Main.class.getClassLoader().getResourceAsStream(queryFileName));
				logger.info(queryString);
				queryDispatchApi.registerQuery(queryFileName, queryString);
			} catch (QueryDispatchException e) {
				logger.warn("Error registering query {} on startup: {}", queryFileName, e.getMessage());
			}
		}
	}

	@Override
	public void stop() throws Exception {
		try {
			// Stop and terminate GCM Components
			logger.info("Terminate application");
			logger.trace("Send stopFc to all components");

			if (root != null) {
				
				// Stop is recursive...
				GCM.getGCMLifeCycleController(root).stopFc();
				// Terminate is not recursive:
				for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
					GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
				}
				GCM.getGCMLifeCycleController(root).terminateGCMComponent();
				
				//TODO stuehmer: use root.join(2000) to wait for shutdown
			}
			
		} catch (IllegalLifeCycleException e) {
			logger.error(e.getMessage());
		} catch (NoSuchInterfaceException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void destroy() {
	
	}
}
