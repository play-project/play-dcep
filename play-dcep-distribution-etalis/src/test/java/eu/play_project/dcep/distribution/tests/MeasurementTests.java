package eu.play_project.dcep.distribution.tests;

import org.junit.Test;

import eu.play_project.dcep.distribution.tests.srbench.performance.MeasurementUnit;


public class MeasurementTests {
	
	@Test
	public void runMeasuremntMethod(){
		MeasurementUnit m = MeasurementUnit.getMeasurementUnit();
		
		m.calcRateForNEvents(10);
		
		for (int i = 0; i < 600; i++) {
			m.nexEvent();
			delay(1000);
		}
	}
	
	public static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
