/**
 * 
 */
package com.espertech.esper.example.transaction.sim;


import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;

/**
 * @author ningyuan 
 * 
 * Apr 30, 2014
 *
 */
public class MapEventWrapper {
	
	public final String name;
	public final MapEvent event;
	
	public MapEventWrapper(String n, MapEvent m){
		name = n;
		event = m;
	}
}
