package eu.play_project.dcep.distribution.eventcloud.remotetests;

import java.io.IOException;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.play_commons.constants.Stream;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;

public class Run {
	
	/**
	 * Send or receive events from EventCloud.
	 * @param args Possible parameters: (subscribe | publish)
	 */

	public static void main(String[] args) throws EventCloudIdNotManaged, IOException {
		
		if (args.length < 1) {
			System.err.println("Possible parameters: (subscribe | publish)");
			System.exit(1);
		}
		
		Logger logger = LoggerFactory.getLogger(Run.class);
		
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
		
		if (args[0].equals("subscribe")) {
			SubscribeForEvents subs =  new SubscribeForEvents();
			
			subs.subscribe("pnp://eventcloud.inria.fr:8081/eventclouds-registry", Stream.SituationalAlertEventStream, Subscription.ACCEPT_ALL);
		
			System.out.println("Press three times enter to terminate application");
			System.in.read();
			subs.unsubscribe();
			System.in.read();
			System.in.read();
		}
		else if (args[0].equals("publish")) {
			EventPublisher eventPublisher = new EventPublisher("pnp://eventcloud.inria.fr:8081/eventclouds-registry", Stream.SituationalEventStream);
			
			eventPublisher.publish(10000, 10);
		
			System.out.println("Press three times enter to terminate application");
			System.in.read();
			System.in.read();
			System.in.read();
		}
		System.out.println("Terminating");
	}

}
