/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;

import java.util.List;


/**
 * Term is a basic component of an expression. It could be either simple event
 * or a time interval.
 * 
 * 
 * 
 * @author ningyuan
 *
 */
public class Term {
	
	/*
	 * the name of event that identifies different events. The object of triple: 	?e	rdf:type	name
	 * property of simple event
	 */
	private final String name;
	
	/*
	 * the name of variable that represent the event in sparql
	 * property of simple event
	 */
	private String varName = null;
	
	/*
	 * the sparql text of the event
	 * property of simple event
	 */
	private String sparqlText;
	
	/*
	 * the duration of time interval
	 * property of time interval
	 */
	private long duration = -1l;
	
	
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
