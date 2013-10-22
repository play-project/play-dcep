package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;


/**
 * Start a single DistributedEtalis instance an register this instance in local registry.
 * @author Stefan Obermeier
 *
 */
public class SingleDistributedEtalisInstanceRunner {
	private static ComplexEventSubscriber subscriber = null;
	private static DistributedEtalisTestApi testApi;
	
	public static void main(String[] args) throws ADLException, IllegalLifeCycleException, NoSuchInterfaceException, ProActiveException, DistributedEtalisException, IOException {
		
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue("proactive.java.policy");
		CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");

		//Start component.
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		Component root = (Component) factory.newComponent("DistributedEtalis", context);
		GCM.getGCMLifeCycleController(root).startFc();

		// Register component.
		Registry registry = LocateRegistry.getRegistry();
		Fractive.registerByName(root, "dEtalis1");
		
		//Configure component.
		ConfigApi configApi = ((ConfigApi)root.getFcInterface(ConfigApi.class.getSimpleName()));
		configApi.setConfig(new DetalisConfigLocal("play-epsparql-clic2call-historical-data.trig"));
		
		
		//Subscribe to print complex events to local console.
//		testApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root.getFcInterface("DistributedEtalisTestApi"));
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
