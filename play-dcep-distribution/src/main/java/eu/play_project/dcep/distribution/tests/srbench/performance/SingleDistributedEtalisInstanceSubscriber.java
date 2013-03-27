package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.IOException;

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

	private static ComplexEventSubscriber subscriber1 = null;
	private static ComplexEventSubscriber subscriber2 = null;
	private static DistributedEtalisTestApi testApiI1;
	private static DistributedEtalisTestApi testApiI2;

	public static void main(String[] args) throws ADLException,
			IllegalLifeCycleException, NoSuchInterfaceException,
			ProActiveException, DistributedEtalisException, IOException, NamingException {

		// Connect to DistributedEtalis instance.
		PAComponentRepresentative root1 = Fractive.lookup(URIBuilder.buildURI(args[0], args[1], "rmi", 1099).toString());
		PAComponentRepresentative root2 = Fractive.lookup(URIBuilder.buildURI(args[2], args[3], "rmi", 1099).toString());

		//Get interfaces.
		testApiI1 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root1.getFcInterface("DistributedEtalisTestApi"));
		testApiI2 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root2.getFcInterface("DistributedEtalisTestApi"));
		
		//Subscribe
		subscriber1 = PAActiveObject.newActive(ComplexEventSubscriber.class, new Object[] {});
		subscriber2 = PAActiveObject.newActive(ComplexEventSubscriber.class, new Object[] {});
		testApiI1.attach(subscriber1);
		testApiI2.attach(subscriber2);
		
		System.out.println("Press 3x RETURN to shutdown the application");
		System.in.read();
		System.in.read();
		System.in.read();
	}
}
