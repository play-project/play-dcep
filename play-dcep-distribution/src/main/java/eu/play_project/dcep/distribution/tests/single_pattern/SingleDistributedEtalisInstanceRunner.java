package eu.play_project.dcep.distribution.tests.single_pattern;

import java.util.HashMap;

import javax.jms.IllegalStateException;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedRequestException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distribution.tests.srbench.performance.ComplexEventSubscriber;


/**
 * Start a single DistributedEtalis instance an register this instance in local registry.
 * @author Stefan Obermeier
 *
 */
public class SingleDistributedEtalisInstanceRunner {
	private static ComplexEventSubscriber subscriber = null;
	private static DistributedEtalisTestApi testApi;
	
	private static Logger logger = LoggerFactory.getLogger(SingleDistributedEtalisInstanceRunner.class);
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
					SingleDistributedEtalisInstanceRunner.stop();
				}
			});

			SingleDistributedEtalisInstanceRunner.start("DistributedEtalis", "dEtalis");
			System.out.println("DCEP is running. Use '${DCEP_HOME}/bin/dcep stop', 'kill' or 'kill -15' to stop it.");

			// Keep the main thread alive because otherwise Proactive will terminate
			synchronized (SingleDistributedEtalisInstanceRunner.class) {
				while (running) {
					try {
						SingleDistributedEtalisInstanceRunner.class.wait();
					}
					catch (InterruptedException e) {}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			SingleDistributedEtalisInstanceRunner.stop();
		}
	}
	
	public static void start(String componentName, String instanceName) throws Exception {
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
		
		root = (Component) factory.newComponent(componentName, context);
		GCM.getGCMLifeCycleController(root).startFc();
		
		// Register component.
		Fractive.registerByName(root, instanceName);
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
			SingleDistributedEtalisInstanceRunner.class.notifyAll();
		}
	}

}
