package eu.play_project.dcep.distribution;

import javax.jms.IllegalStateException;

import org.apache.commons.io.IOUtils;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);
	private static boolean running;
	private static Component root;

	/**
	 * Start DCEP. There are several ways to stop it: (1) by killing the main
	 * thread a shutdownhook is caught (2) when an exception happens during
	 * start.
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

			Main.start("PlayPlatform");
			System.out.println("DCEP is running. Use '${DCEP_HOME}/bin/dcep stop', 'kill' or 'kill -15' to stop it.");

			// Keep the main thread alive because otherwise Proactive will
			// terminate
			synchronized (Main.class) {
				while (running) {
					try {
						Main.class.wait();
					}
					catch (InterruptedException e) {}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			Main.stop();
		}

	}

	/**
	 * Start DCEP with a {@code *.fractal} file name.
	 * 
	 * A few configuration parameters for ProActive are in the DCEP Constants
	 * file {@code play-dcep-distribution.properties} (e.g. the ones which
	 * differ for Unit Tests). For the rest see the file
	 * {@code ProActiveConfiguration.xml}.
	 */
	public static void start(String componentName) throws Exception {

		/*
		 * Set up Components
		 */
		root = ProActiveHelpers.newComponent(componentName);

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
		QueryDispatchApi queryDispatchApi = ((QueryDispatchApi) root
				.getFcInterface(QueryDispatchApi.class.getSimpleName()));

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
					logger.warn("Error registering query '{}' on startup: {}",
							queryFileName, e.getMessage());
				} catch (NullPointerException e) {
					logger.warn("Error registering query '{}' on startup: {}",
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
