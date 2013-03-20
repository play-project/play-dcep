package eu.play_project.dcep.distribution.tests.single_pattern;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;

import javax.naming.NamingException;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;

public class SingleDistributedEtalisInstanceSubscriber {

	private static ComplexEventSubscriber subscriber = null;
	private static DistributedEtalisTestApi testApiI1;

	public static void main(String[] args) throws ADLException,
			IllegalLifeCycleException, NoSuchInterfaceException,
			ProActiveException, DistributedEtalisException, IOException, NamingException {

		// Connect to DistributedEtalis instance.
		PAComponentRepresentative root1 = Fractive.lookup(URIBuilder.buildURI(args[0], args[1], "rmi", 1099).toString());

		//Get interfaces.
		testApiI1 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root1.getFcInterface("DistributedEtalisTestApi"));
		
		//Subscribe
		subscriber = PAActiveObject.newActive(ComplexEventSubscriber.class, new Object[] {});
		testApiI1.attach(subscriber);
		
		System.out.println("Press 3x RETURN to shutdown the application");
		System.in.read();
		System.in.read();
		System.in.read();
	}
}
