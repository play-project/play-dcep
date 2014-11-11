package eu.play_project.dcep.distribution.tests.single_pattern;

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

import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distribution.tests.srbench.performance.ComplexEventSubscriber;
import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.dcep.node.api.DcepNodeException;
import fr.inria.eventcloud.api.CompoundEvent;

public class SingleDistributedEtalisInstanceSubscriber {

	private static ComplexEventSubscriber subscriber = null;
	private static DcepNodeApi<CompoundEvent> testApiI1;

	public static void main(String[] args) throws ADLException,
			IllegalLifeCycleException, NoSuchInterfaceException,
			ProActiveException, DcepNodeException, IOException, NamingException, DcepManagementException {

		// Connect to DistributedEtalis instance.
		connectToCepEngine("dEtalis", args[0]);
	
		//Subscribe
		subscriber = PAActiveObject.newActive(ComplexEventSubscriber.class, new Object[] {});
		testApiI1.attach(subscriber);
		
		System.out.println("Press 3x RETURN to shutdown the application");
		System.in.read();
		System.in.read();
		System.in.read();
	}
	
	private static void connectToCepEngine(String name, String host) throws IOException, NamingException, DcepManagementException, DcepNodeException{

		/* COMPONENT_ALIAS = "Dispatcher" */
		PAComponentRepresentative root = null;

		try {
			root = Fractive.lookup((URIBuilder.buildURI(host, name, "pnp", Integer.parseInt(DcepConstants.getProperties().getProperty("dcep.proactive.pnp.port"))).toString()));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}

		try {
			 testApiI1 = ((DcepNodeApi<CompoundEvent>) root.getFcInterface(DcepNodeApi.class.getSimpleName()));

		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
	}
}