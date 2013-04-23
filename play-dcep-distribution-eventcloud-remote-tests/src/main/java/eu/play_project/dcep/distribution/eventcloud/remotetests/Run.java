package eu.play_project.dcep.distribution.eventcloud.remotetests;

import java.io.IOException;

import eu.play_project.play_commons.constants.Stream;
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
		
		if (args[0].equals("subscribe")) {
			SubscribeForEvents subs =  new SubscribeForEvents();
			
			subs.subscribe("pnp://eventcloud.inria.fr:8081/eventclouds-registry", Stream.TaxiUCCall, "SELECT ?id1 ?s ?p ?o WHERE { GRAPH ?id1 { ?s ?p ?o }}");
		
			System.out.println("Press three times enter to terminate application");
			System.in.read();
			subs.unsubscribe();
			System.in.read();
		}
		else if (args[0].equals("publish")) {
			EventPublisher eventPublisher = new EventPublisher("pnp://eventcloud.inria.fr:8081/eventclouds-registry", Stream.TaxiUCCall.getTopicUri());
			
			eventPublisher.publish(10000, 10);
		
			System.out.println("Press three times enter to terminate application");
			System.in.read();
			System.in.read();
		}
	}

}
