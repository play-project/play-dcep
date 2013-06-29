package eu.play_project.dcep.distributedetalis.test;

import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import eu.play_project.dcep.api.measurement.PatternMeasuringResult;

public class MeasurementTests {
	static Component root;
	DcepMonitoringApi dEtalis;

	@Before
	public void instantiateDcepComponent() throws ADLException, IllegalLifeCycleException, NoSuchInterfaceException, InterruptedException {
		
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(System.getProperty("user.dir") + "\\src\\main\\resources\\proactive.java.policy");
		CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");

		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		root = (Component) factory.newComponent("DistributedEtalis", context);
		GCM.getGCMLifeCycleController(root).startFc();
		
		 dEtalis = ((eu.play_project.dcep.api.DcepMonitoringApi) root.getFcInterface("DcepMonitoringApi"));
		
		Thread.sleep(10000);
	}
	
	public static void print(NodeMeasuringResult result){
		System.out.println("---------------------------------------------------");
		System.out.println("Proces time for one event: " +result.getProcessingTimeForOneEvent());
		for (PatternMeasuringResult key : result.getMeasuredValues()) {
			System.out.println(key.getName() + "  " + (key.getProcessedEvents()/4));
		}
		System.out.println("Component input  events: " + result.getNumberOfComponentInputEvetns());
		System.out.println("Etalis input events: " + result.getNumberOfEtalisInputEvents());
		System.out.println("Output events: " + result.getNumberOfOutputEvents());
		System.out.println("---------------------------------------------------");
	}

}
