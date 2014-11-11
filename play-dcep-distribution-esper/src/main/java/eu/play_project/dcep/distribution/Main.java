package eu.play_project.dcep.distribution;

import javax.jms.IllegalStateException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.Dcep;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

public class Main {

	private static boolean running;
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	private static Dcep root;

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

			Main.start();
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
	 * Start DCEP.
	 * 
	 * A few configuration parameters for DCEP are in the DCEP Constants
	 * file {@code play-dcep-distribution.properties} (e.g. the ones which
	 * differ for Unit Tests).
	 */
	public static void start() throws Exception {

		/*
		 * Set up Components
		 */
		Dcep root = new Dcep();


		// Get interfaces
		QueryDispatchApi queryDispatchApi = null; //FIXME stuehmer

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

				// FIXME stuehmer
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			running = false;
			Main.class.notifyAll();
		}
	}

}
