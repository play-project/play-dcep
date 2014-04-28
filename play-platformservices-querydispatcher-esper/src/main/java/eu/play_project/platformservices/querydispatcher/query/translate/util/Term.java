/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate.util;


/**
 * @author ningyuan
 *
 */

/*
 * event
 * time interval
 * 
 */
public class Term {
	
	
	private final String name;
	
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
	 * Constructor simple event
	 */
	public Term(String n){
		name = n;
	}
	
	
	public String getName(){
		return name;
	}
	
}
