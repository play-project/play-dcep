/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;

/**
 * @author ningyuan 
 * 
 * Apr 4, 2014
 *
 */
public class Entry {
	
	private Term not;
	private Term notEnd;
	
	public Entry(Term not, Term notEnd){
		this.not = not;
		this.notEnd = notEnd;
	}
	
	public Term getNot() {
		return not;
	}
	public void setNot(Term not) {
		this.not = not;
	}
	public Term getNotEnd() {
		return notEnd;
	}
	public void setNotEnd(Term notEnd) {
		this.notEnd = notEnd;
	}
	
	
	
}
