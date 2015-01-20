/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.realtime.util;

import java.util.ArrayList;
import java.util.List;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.BDPLArrayFilter;



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
	private String idVarName = null;
	
	/*
	 * the name of variable that represent object of :endTime
	 * property of simple event
	 */
	private String endTimeVarName = null;
	
	/*
	 * the sparql text of the event
	 * property of simple event
	 */
	private String sparqlText;
	
	/*
	 * list of bdpl filters
	 * property of simple event
	 */
	private List<BDPLArrayFilter> filters = new ArrayList<BDPLArrayFilter>();
	
	
	
	/*
	 * the duration of time interval
	 * property of time interval
	 */
	private long duration = -1l;
	
	
	/*
	 * Constructor time based event
	 */
	public Term(String t){
		name = t;
	}
	
	/*
	 * Constructor simple event
	 */
	public Term(String name, String idVarName, String endTimeVarName){
		this.name = name;
		this.idVarName = idVarName;
		this.endTimeVarName = endTimeVarName;
	}
	
	
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
	
	public String getIDVarName(){
		return idVarName;
	}
	
	public String getName(){
		return name;
	}
	
	public String getEndTimeVarName() {
		return this.endTimeVarName;
	}
	
	public List<BDPLArrayFilter> getFilters() {
		return this.filters;
	}

}
