package eu.play_project.dcep.distribution.examples;

import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;
import fr.inria.eventcloud.api.PublishApi;

public class Distibutor {

	// " java -Djava.security.policy=proactive.java.policy -Dproactive.home=/tmp/ProActiveProgramming-5.3.2_core_bin -cp '/tmp/ProActiveProgramming-5.3.2_core_bin/dist/lib/*' org.objectweb.proactive.examples.components.userguide.starter.Main";
	private static PublishApi dcepPublishApi;
	private static DcepManagmentApi dcepManagmentApi = null;

	public static void main(String[] args) throws Exception {
		
		VariableContractImpl variableContract = new VariableContractImpl();
		// variableContract.setVariableFromProgram("HOSTS",
		// "detalis1.s-node.de", VariableContractType.DescriptorVariable);


		CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
		
		// Start node
		GCMApplication gcma = PAGCMDeployment
				.loadApplicationDescriptor(Distibutor.class
						.getResource("/dEtalisApplicationDescriptor.xml"));
		gcma.startDeployment();

		GCMVirtualNode vn = gcma.getVirtualNode("dEtalis-node");
		vn.waitReady();

		// Start component.
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, GCMApplication> context = new HashMap<String, GCMApplication>(1);
		context.put("deployment-descriptor", gcma);

		Component root = (Component) factory.newComponent("DistributedEtalis", context);
		GCM.getGCMLifeCycleController(root).startFc();

		dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root.getFcInterface("DcepManagmentApi"));

		ConfigApi configApi = ((ConfigApi) root.getFcInterface("ConfigApi"));
		configApi.setConfig(new DetalisConfigLocal("play-epsparql-clic2call-historical-data.trig"));

		// Register apis
		java.rmi.registry.Registry registry = LocateRegistry.getRegistry();

		Fractive.registerByName(root, "dEtalis");

		// bind components
		// BindingController bc = GCM.getBindingController(client);
		// bc.bindFc("s", server.getFcInterface("s"));

		// start components
		// GCM.getGCMLifeCycleController(server).startFc();
		// GCM.getGCMLifeCycleController(client).startFc();

		// launch the application
		// ((Runnable) client.getFcInterface("m")).run();

		System.out.println("Press 3x RETURN to shutdown the application");
		System.in.read();
		System.in.read();
		System.in.read();
		
		// stop components
		GCM.getGCMLifeCycleController(root).stopFc();
		
		Registry.instance().clear();
		gcma.kill();
	}
}
