/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;

import java.util.List;

/**
 * @author ningyuan
 *
 */

/*
 * event
 * time interval
 * sequence events
 * 
 */
public class Term {
	
	
	private final String type;
	
	// id in event table
	private int eventIDs = -1;
	
	private String duration = null;

	public String getDuration() {
		return duration;
	}


	public void setDuration(String duration) {
		this.duration = duration;
	}


	/*
	 * Constructor simple event
	 */
	public Term(String t, int eids){
		type = t;
		eventIDs = eids;
	}
	
	
	public String getType(){
		return type;
	}
	
}
