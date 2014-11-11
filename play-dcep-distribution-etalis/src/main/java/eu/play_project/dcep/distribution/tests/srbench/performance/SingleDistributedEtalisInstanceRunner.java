package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;

import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.dcep.node.api.DcepNodeConfiguringApi;
import eu.play_project.dcep.node.api.DcepNodeException;
import fr.inria.eventcloud.api.CompoundEvent;


/**
 * Start a single DistributedEtalis instance an register this instance in local registry.
 * @author Stefan Obermeier
 *
 */
public class SingleDistributedEtalisInstanceRunner {
	private static ComplexEventSubscriber subscriber = null;
	private static DcepNodeApi<CompoundEvent> testApi;
	
	public static void main(String[] args) throws ADLException, IllegalLifeCycleException, NoSuchInterfaceException, ProActiveException, DcepNodeException, IOException, DcepManagementException {
		
		//Start component.
		Component root = ProActiveHelpers.newComponent("DistributedEtalis");
		GCM.getGCMLifeCycleController(root).startFc();

		// Register component.
		Registry registry = LocateRegistry.getRegistry();
		Fractive.registerByName(root, "dEtalis");
		
		//Configure component.
		DcepNodeConfiguringApi<CompoundEvent> configApi = ((DcepNodeConfiguringApi<CompoundEvent>)root.getFcInterface(DcepNodeConfiguringApi.class.getSimpleName()));
		configApi.setConfig("play-epsparql-clic2call-historical-data.trig");
		
		
		//Subscribe to print complex events to local console.
//		testApi = ((eu.play_project.dcep.distributedetalis.api.DcepTestApi) root.getFcInterface(DcepTestApi.class.getSimpleName()));
//		try {
//			subscriber = PAActiveObject.newActive(ComplexEventSubscriber.class, new Object[] {});
//		} catch (ActiveObjectCreationException e) {
//			e.printStackTrace();
//		} catch (NodeException e) {
//			e.printStackTrace();
//		}
//		testApi.attach(subscriber);
		
		System.out.println("Press 3x RETURN to shutdown the application");
		System.in.read();
		System.in.read();
		System.in.read();
	}

}
