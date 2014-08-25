/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.event;

import java.util.HashMap;


/**
 * The underlying event object used in Esper engine. Each MapEvent has a property EVENT_MODEL,
 * which refers to an EventModel.
 * 
 * 
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class MapEvent <T extends EventModel> extends HashMap<String, T>{
	
	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_MODEL = "model";
	
	public MapEvent(T model){
		if(model == null)
			throw new IllegalArgumentException();
		
		this.put(EVENT_MODEL, model);
	}
	
}
