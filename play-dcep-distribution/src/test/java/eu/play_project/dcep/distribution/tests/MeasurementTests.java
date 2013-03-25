package eu.play_project.dcep.distribution.tests;

import org.junit.Test;

import eu.play_project.dcep.distribution.tests.single_pattern.Measurement;

public class MeasurementTests {
	
	@Test
	public void runMeasuremntMethod(){
		Measurement m = new Measurement();
		
		m.calcRateForNevents(10);
		
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
