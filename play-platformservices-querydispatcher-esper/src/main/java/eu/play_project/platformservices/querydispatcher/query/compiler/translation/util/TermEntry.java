/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;

/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class TermEntry {
	
	private final Term term;
	
	private String RDFRelation = "";
	
	public TermEntry(Term term){
		this.term = term;
	}
	
	public Term getTerm(){
		return term;
	}
	
	public String getRDFRelation(){
		return RDFRelation;
	}
	
	public void setRDFRelation(String r){
		RDFRelation = r;
	}
}
