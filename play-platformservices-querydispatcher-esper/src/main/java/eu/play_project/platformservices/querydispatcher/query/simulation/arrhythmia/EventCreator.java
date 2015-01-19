/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia;

import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;

/**
 * @author ningyuan 
 * 
 * Dec 15, 2014
 *
 */
public abstract class EventCreator {
	
	protected final String eventType;
	
	public EventCreator(String eventType){
		this.eventType = eventType;
	}
	
	public String getEventType(){
		return eventType;
	}
	
	abstract public MapEvent next();
	
	abstract public void close();
}
