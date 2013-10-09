package eu.play_project.dcep.distribution;

import java.util.HashMap;

import javax.jms.IllegalStateException;

import org.apache.commons.io.IOUtils;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedRequestException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);
	private static boolean running;
	private static Component root;

	/**
	 * Start DCEP. There are several ways to stop it: (1) by killing the main
	 * thread a shutdownhook is caught (2) by sending 3 newlines on stdin (3)
	 * when an exception happens during start.
	 */
	public static void main(String[] args) throws IllegalStateException {
		if (running) {
			throw new IllegalStateException("Already running...");
		}
		
		running = true;

		try {

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					logger.info("Shutting down...");
					Main.stop();
				}
			});

			Main.start();
			System.out.println("For now DCEP cannot be stopped by pressing 3x RETURN, use 'kill' or 'kill -15' instead");

			// Keep the main thread alive because otherwise Proactive will terminate
			synchronized (Main.class) {
				while (running) {
					try {
						Main.class.wait();
					}
					catch (InterruptedException e) {}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			Main.stop();
		}

	}

	public static void start() throws Exception {
		final String PROACTIVE_PNP_PORT = DcepConstants.getProperties().getProperty("dcep.proactive.pnp.port");
		final String PROACTIVE_HTTP_PORT = DcepConstants.getProperties().getProperty("dcep.proactive.http.port");
		final String PROACTIVE_RMI_PORT = DcepConstants.getProperties().getProperty("dcep.proactive.rmi.port");
		final String PROACTIVE_COMMUNICATION_PROTOCOL = "pnp";
		
		logger.debug("Setting system property 'proactive.communication.protocol' to: " + PROACTIVE_COMMUNICATION_PROTOCOL);
		CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.setValue(PROACTIVE_COMMUNICATION_PROTOCOL);

		logger.debug("Setting system property 'proactive.pnp.port' to: " + PROACTIVE_PNP_PORT);
		PNPConfig.PA_PNP_PORT.setValue(Integer.parseInt(PROACTIVE_PNP_PORT));
		
		logger.debug("Setting system property 'proactive.http.port' to: " + PROACTIVE_HTTP_PORT);
		CentralPAPropertyRepository.PA_XMLHTTP_PORT.setValue(Integer.parseInt(PROACTIVE_HTTP_PORT));
		
		logger.debug("Setting system property 'proactive.rmi.port' to: " + PROACTIVE_RMI_PORT);
		CentralPAPropertyRepository.PA_RMI_PORT.setValue(Integer.parseInt(PROACTIVE_RMI_PORT));
		
		logger.debug("Setting system property 'proactive.runtime.ping' to: false");
		CentralPAPropertyRepository.PA_RUNTIME_PING.setValue(false);
		
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
				.setValue("proactive.java.policy");

		CentralPAPropertyRepository.GCM_PROVIDER
				.setValue(org.objectweb.proactive.core.component.Fractive.class.getName());

		/*
		 * Set up Components
		 */
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		root = (Component) factory.newComponent("PlayPlatform", context);

		GCM.getGCMLifeCycleController(root).startFc();

		boolean init = false;

		// Wait for all subcomponents to be started
		while (!init) {
			boolean overallInitStatus = true;
			for (Component subcomponent : GCM.getContentController(root)
					.getFcSubComponents()) {
				overallInitStatus = overallInitStatus
						&& GCM.getGCMLifeCycleController(subcomponent)
								.getFcState()
								.equals(LifeCycleController.STARTED);
			}
			if (overallInitStatus == true) {
				init = true;
			} else {
				logger.info("Wait for all subcomponents to be started...");
				Thread.sleep(500);
			}

		}

		// Get interfaces
		QueryDispatchApi queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root
				.getFcInterface("QueryDispatchApi"));

		/*
		 * Compile and Deploy Queries
		 */
		for (String queryFileName : DcepConstants.getProperties()
				.getProperty("dcep.startup.registerqueries").split(",")) {
			queryFileName = queryFileName.trim();
			if (!queryFileName.isEmpty()) {
				try {
					String queryString = IOUtils.toString(Main.class
							.getClassLoader().getResourceAsStream(queryFileName));
					logger.info(queryString);
					queryDispatchApi.registerQuery(queryFileName, queryString);
				} catch (QueryDispatchException e) {
					logger.warn("Error registering query {} on startup: {}",
							queryFileName, e.getMessage());
				}
			}
		}
	}

	public static synchronized void stop() {
		try {
			// Stop and terminate GCM Components
			logger.info("Terminate application");
			logger.debug("Send stopFc to all components");

			if (root != null) {

				// Stop is recursive...
				try {
					GCM.getGCMLifeCycleController(root).stopFc();
				} catch (IllegalLifeCycleException e) {
					logger.error(e.getMessage());
				} catch (BodyTerminatedRequestException e) {
					logger.error(e.getMessage());
				}
				
				// Terminate is not recursive:
				for (Component subcomponent : GCM.getContentController(root)
						.getFcSubComponents()) {
					try {
						GCM.getGCMLifeCycleController(subcomponent)
								.terminateGCMComponent();
					} catch (IllegalLifeCycleException e) {
						logger.error(e.getMessage());
					} catch (BodyTerminatedRequestException e) {
						logger.error(e.getMessage());
					}
				}
				try {
					GCM.getGCMLifeCycleController(root).terminateGCMComponent();
				} catch (IllegalLifeCycleException e) {
					logger.error(e.getMessage());
				}
			}

		} catch (NoSuchInterfaceException e) {
			logger.error(e.getMessage());
		} finally {
			running = false;
			Main.class.notifyAll();
		}
	}
}
