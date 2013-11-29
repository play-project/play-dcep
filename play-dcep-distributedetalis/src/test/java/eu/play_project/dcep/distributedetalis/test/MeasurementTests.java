package eu.play_project.dcep.distributedetalis.test;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.api.measurement.PatternMeasuringResult;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;

public class MeasurementTests {
	static Component root;
	DcepMonitoringApi dEtalis;

	@Before
	public void instantiateDcepComponent() throws ADLException, IllegalLifeCycleException, NoSuchInterfaceException, InterruptedException {

		root = ProActiveHelpers.newComponent("DistributedEtalis");
		GCM.getGCMLifeCycleController(root).startFc();
		
		 dEtalis = ((eu.play_project.dcep.api.DcepMonitoringApi) root.getFcInterface(DcepMonitoringApi.class.getSimpleName()));
		
		Thread.sleep(10000);
	}
	
	public static void print(NodeMeasurementResult result){
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
