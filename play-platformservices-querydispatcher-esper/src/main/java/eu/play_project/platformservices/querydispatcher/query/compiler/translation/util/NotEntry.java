/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;

/**
 * This class describes a pattern A -> B and not C, which Term A
 * is notStart, B is notEnd and C is not.
 * 
 * 
 * @author ningyuan 
 * 
 * Apr 4, 2014
 *
 */
public class NotEntry implements IEntry {
	
	private Term notStart;
	private Term not;
	private Term notEnd;
	
	public NotEntry(Term notStart, Term not, Term notEnd){
		this.notStart = notStart;
		this.not = not;
		this.notEnd = notEnd;
	}
	
	public Term getNotStart() {
		return this.notStart;
	}

	public void setNotStart(Term notStart) {
		this.notStart = notStart;
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
