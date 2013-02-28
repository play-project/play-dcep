package eu.play_project.dcep.distribution;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisLocalConfig;
import eu.play_project.dcep.distributedetalis.test.PublishApiSubscriber;
import fr.inria.eventcloud.api.PublishApi;

/**
 * Start a single DistributedEtalis instance an register this instance in local registry.
 * @author Stefan Obermeier
 *
 */
public class SingleDistributedEtalisInstanceRunner {
	private static PublishApiSubscriber subscriber = null;
	private static DistributedEtalisTestApi testApi;
	
	public static void main(String[] args) throws ADLException, IllegalLifeCycleException, NoSuchInterfaceException, RemoteException, ProActiveException, DistributedEtalisException {
		
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
		ConfigApi configApi = ((ConfigApi)root.getFcInterface("ConfigApi"));
		configApi.setConfig(new DetalisLocalConfig("play-epsparql-clic2call-historical-data.trig"));
		
		
		//Subscribe to print complex events to local console.
		testApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root.getFcInterface("DistributedEtalisTestApi"));
		try {
			subscriber = PAActiveObject.newActive(PublishApiSubscriber.class, new Object[] {});
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}
		testApi.attach(subscriber);
	}

}
