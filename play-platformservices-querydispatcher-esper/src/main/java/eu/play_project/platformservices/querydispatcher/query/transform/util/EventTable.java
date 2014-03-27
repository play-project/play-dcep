/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ningyuan
 *
 */
public class EventTable {
	
	private Map<Integer, EventEntry> map = new HashMap<Integer, EventEntry>();
	
	public EventEntry put(int key, EventEntry value){
		return map.put(key, value);
	}
	
	public EventEntry get(int key){
		return map.get(key);
	}
	
	public int size(){
		return map.size();
	}
}
