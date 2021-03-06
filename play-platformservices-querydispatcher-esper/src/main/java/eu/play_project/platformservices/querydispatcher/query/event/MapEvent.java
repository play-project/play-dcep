/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.event;

import java.util.HashMap;


/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class MapEvent <T extends EventModel> extends HashMap<String, T>{
	
	public static final String EVENT_MODEL = "model";
	
	public long timestamp = -1l;
	
	public MapEvent(T model){
		if(model == null)
			throw new IllegalArgumentException();
		
		this.put(EVENT_MODEL, model);
	}
	
	public void setTimeStamp(long t){
		timestamp = t;
	}
	
	public long getTimeStamp(){
		return timestamp;
	}
}
