/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame;


import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;



/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class SesameMapEvent extends MapEvent<SesameEventModel>{
	
	public SesameMapEvent(SesameEventModel model){
		super(model);
	}
}