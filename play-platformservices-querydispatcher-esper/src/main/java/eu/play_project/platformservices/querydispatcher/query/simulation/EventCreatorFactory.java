/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;

import eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia.MITECGEventCreator1;
import eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia.RREventCreator;

/**
 * @author ningyuan 
 * 
 * Mar 5, 2015
 *
 */
public class EventCreatorFactory {
	

	public static final String TYPE_ECG = "ecg", TYPE_RR = "rr";
	
	static EventCreator getEventCreator(String t){
		
		if(t.equalsIgnoreCase(TYPE_ECG)){
			return new MITECGEventCreator1();
		}
		else if(t.equalsIgnoreCase(TYPE_RR)){
			return new RREventCreator();
		}
		else{
			return null;
		}
	}
}
