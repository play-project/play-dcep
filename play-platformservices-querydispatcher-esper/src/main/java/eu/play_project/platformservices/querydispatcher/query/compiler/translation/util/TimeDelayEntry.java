/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;

/**
 * This class describes time delays: START -> DURATION -> END.
 * The DURATION starts direct after event START happening and ends before
 * event END occuring.
 * 
 *  
 * 
 * @author ningyuan 
 * 
 * Apr 7, 2014
 *
 */
public class TimeDelayEntry implements IEntry{
	
	private Term start;
	
	private long duration = -1;
	
	private Term end;
	
	public TimeDelayEntry(Term start, Term end, long duration){
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

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Term getEnd() {
		return end;
	}

	public void setEnd(Term end) {
		this.end = end;
	}

	
}
