package eu.play_project.dcep.distribution.examples;

import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.dcep.node.api.DcepNodeConfiguringApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;

public class Distibutor {

	// " java -Djava.security.policy=proactive.java.policy -Dproactive.home=/tmp/ProActiveProgramming-5.3.2_core_bin -cp '/tmp/ProActiveProgramming-5.3.2_core_bin/dist/lib/*' org.objectweb.proactive.examples.components.userguide.starter.Main";
	private static PublishApi dcepPublishApi;
	private static DcepManagmentApi dcepManagmentApi = null;

	public static void main(String[] args) throws Exception {
		
		VariableContractImpl variableContract = new VariableContractImpl();
		// variableContract.setVariableFromProgram("HOSTS",
		// "detalis1.s-node.de", VariableContractType.DescriptorVariable);

		// Start node
		GCMApplication gcma = PAGCMDeployment.loadApplicationDescriptor(Distibutor.class
						.getResource("/dEtalisApplicationDescriptor-1.xml"));
		gcma.startDeployment();

		GCMVirtualNode vn = gcma.getVirtualNode("dEtalis-node");
		vn.waitReady();

		// Start component.
		Map<String, Object> context = new HashMap<String, Object>(1);
		context.put("deployment-descriptor", gcma);

		Component root = ProActiveHelpers.newComponent("DistributedEtalis", context);
		GCM.getGCMLifeCycleController(root).startFc();

		dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root
				.getFcInterface(DcepManagmentApi.class.getSimpleName()));

		DcepNodeConfiguringApi<CompoundEvent> configApi = ((DcepNodeConfiguringApi<CompoundEvent>) root.getFcInterface(DcepNodeConfiguringApi.class.getSimpleName()));
		configApi.setConfigLocal("play-epsparql-clic2call-historical-data.trig");

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
