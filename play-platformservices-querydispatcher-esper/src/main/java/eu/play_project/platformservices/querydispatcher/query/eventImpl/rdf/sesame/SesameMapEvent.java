/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.eventImpl.rdf.sesame;


import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;



/**
 * @author ningyuan
 * 
 * Apr 16, 2014
 *
 */
public class SesameMapEvent extends MapEvent<SesameEventModel>{
	
	private static final long serialVersionUID = 6601641969543197526L;

	public SesameMapEvent(SesameEventModel model){
		super(model);
	}
}
