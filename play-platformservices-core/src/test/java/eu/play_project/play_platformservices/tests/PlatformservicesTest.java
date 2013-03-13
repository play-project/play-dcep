package eu.play_project.play_platformservices.tests;

import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import eu.play_project.play_platformservices.PlayPlatformservices;

public class PlatformservicesTest {

	@Test
	public void testCxfSoap() {
		
		PlayPlatformservices playPlatformservices = new PlayPlatformservices();
		playPlatformservices.initComponentActivity(null);
		playPlatformservices.endComponentActivity(null);
		
	}
	
	@Test
	public void testPlatformservicesComponent() throws ADLException, IllegalLifeCycleException,
			NoSuchInterfaceException, InterruptedException {
		
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
		.setValue("proactive.java.policy");

		CentralPAPropertyRepository.GCM_PROVIDER
		.setValue("org.objectweb.proactive.core.component.Fractive");
		
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();
		
		Component root = (Component) factory.newComponent("PlatformServicesTest", context);
		GCM.getGCMLifeCycleController(root).startFc();
		GCM.getGCMLifeCycleController(root).stopFc();
		GCM.getGCMLifeCycleController(root).terminateGCMComponent();
	}

}
