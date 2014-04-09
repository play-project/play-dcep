/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;

/**
 * @author ningyuan 
 * 
 * Apr 7, 2014
 *
 */
public class TimeDelayEntry {
	
	private Term start;
	
	private String duration;
	
	private Term end;
	
	public TimeDelayEntry(Term start, Term end, String duration){
		this.start = start;
		this.end = end;
		this.duration = duration;
	}
	
	public Term getStart() {
		return start;
	}

	public void setStart(Term start) {
		this.start = start;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public Term getEnd() {
		return end;
	}

	public void setEnd(Term end) {
		this.end = end;
	}

	
}
