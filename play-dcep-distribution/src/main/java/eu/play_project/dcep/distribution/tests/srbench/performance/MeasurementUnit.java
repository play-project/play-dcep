package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.Serializable;

/**
 * Measure eventrate calculated by counting events and reading System.nanoTime().
 * @author sobermeier
 *
 */

public class MeasurementUnit implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 100L;
	private long numberOfEvents = 0;
	private long startTime = 0;
	private long n;
	private static MeasurementUnit unit = new MeasurementUnit();
	
	private MeasurementUnit(){}
	
	public static MeasurementUnit getMeasurementUnit(){
		return unit;
	}
	
	public void startNewMeasuringPeriod(){
		startTime = System.nanoTime();
		numberOfEvents = 0;
	}
	
	public void calcRateForNEvents(long n){
		this.n = n;
		startNewMeasuringPeriod();
	}
	
	public synchronized void nexEvent(){
		numberOfEvents++;
		
		if((numberOfEvents%n)==0){
			System.out.println(System.currentTimeMillis() + "\t" +getEventsPerSecond());
			startTime = System.nanoTime();
			numberOfEvents = 0;
		}
	}

	
	public void reset(){
		startTime = 0;
		numberOfEvents = 0;
	}
	
	public double getEventsPerSecond(){
		return numberOfEvents / ((System.nanoTime() - startTime)/1000000000.0);	
	}

}
