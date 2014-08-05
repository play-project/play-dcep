/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;


/**
 * Term is a basic component of an expression. It could be either simple event
 * with an attribute sparqlText or a time interval with an attribute duration.
 * 
 * 
 * 
 * @author ningyuan
 *
 */
public class Term {
	
	
	private final String name;
	
	private String varName = null;
	
	private long duration = -1l;
	
	private String sparqlText;
	

	public String getSparqlText(){
		return sparqlText;
	}
	
	public void setSparqlText(String s){
		sparqlText = s;
	}
	
	public long getDuration() {
		return duration;
	}


	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	/*
	 * Constructor time based event
	 */
	public Term(String t){
		name = t;
	}
	
	/*
	 * Constructor simple event
	 */
	public Term(String vn, String t){
		varName = vn;
		name = t;
	}
	
	public String getVarName(){
		return varName;
	}
	
	public String getName(){
		return name;
	}
	
}
