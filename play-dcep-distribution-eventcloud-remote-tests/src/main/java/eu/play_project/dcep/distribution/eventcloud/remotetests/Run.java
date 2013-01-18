package eu.play_project.dcep.distribution.eventcloud.remotetests;

import java.io.IOException;
import java.util.Scanner;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_commons.constants.Stream;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;

public class Run {

	public static void main(String[] args) throws EventCloudIdNotManaged, IOException {
		
		if (args.length < 1) {
			System.err.println("Possible parameters: subscribe");
			System.exit(1);
		}
		
		if (args[0].equals("subscribe")) {
			SubscribeForEvents subs =  new SubscribeForEvents();
			
			subs.subscribe(Constants.getProperties().getProperty("eventcloud.registry"), Stream.TaxiUCCall, "SELECT ?id1 ?s ?p ?o WHERE { GRAPH ?id1 { ?s ?p ?o }}");
		
			System.out.println("Press three times enter to terminate application");
			System.in.read();
			subs.unsubscribe();
			System.in.read();
		}
	}

}
