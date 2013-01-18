package eu.play_project.dcep.distribution.tests;

import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.SubscribeApi;

public class ConnectionToEventCloud {
	private static final String propertiesFile = "proactive.java.policy";

	/**
	 * Start EC component and stop it.
	 */
	public static void main(String[] args) throws Exception {
		//Logger  logger = Logger.getRootLogger();
		//logger.setLevel(Level.INFO);
		/*
		 * Set up Components
		 */
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(propertiesFile);
		CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");

		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		Component root = (Component) factory.newComponent("EC", context);

		// Start component
		GCM.getGCMLifeCycleController(root).startFc();

		// Get interfaces
		PublishApi ecPublishApi = (fr.inria.eventcloud.api.PublishApi) root.getFcInterface("PublishApi");
		SubscribeApi ecSubscribeApi = (fr.inria.eventcloud.api.SubscribeApi) root.getFcInterface("SubscribeApi");

		Thread.sleep(5000);
		
		//logger.info("Terminate EC");
		// Stop and terminate components
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			// Terminate all subcomponents.
			for (Component subcomponent : GCM.getContentController(root).getFcSubComponents()) {
				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
			}
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

	}
}
