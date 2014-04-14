/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;


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
	
	
	private final String name;
	
	// id in event table
	private int eventIDs = -1;
	
	private long duration = -1l;
	
	public long getDuration() {
		return duration;
	}


	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Term(String n){
		name = n;
	}
	
	/*
	 * Constructor simple event
	 */
	public Term(String n, int eids){
		name = n;
		eventIDs = eids;
	}
	
	
	public String getName(){
		return name;
	}
	
}
