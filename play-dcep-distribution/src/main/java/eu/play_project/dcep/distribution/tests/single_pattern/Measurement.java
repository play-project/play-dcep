package eu.play_project.dcep.distribution.tests.single_pattern;

/**
 * Measure eventrate calculated by counting events and reading System.nanoTime().
 * @author sobermeier
 *
 */

public class Measurement {
	
	private long numberOfEvents = 0;
	private long startTime = 0;
	private long n;
	
	public void startNewMeasuringPeriod(){
		startTime = System.nanoTime();
		numberOfEvents = 0;
	}
	
	public void calcRateForNevents(long n){
		this.n = n;
		startNewMeasuringPeriod();
	}
	
	public void nexEvent(){
		numberOfEvents++;
		
		if((numberOfEvents%n)==0){
			System.out.println(getEventsPerSecond());
			startTime = System.nanoTime();
			numberOfEvents = 0;
		}
	}

	
	public void reset(){
		startTime = 0;
		numberOfEvents = 0;
	}
	
	public double getEventsPerSecond(){
		return numberOfEvents / ((System.nanoTime() - startTime)/1000000000);	
	}

}
