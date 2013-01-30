package eu.play_project.dcep.distributedetalis;

import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;

public class EcConnectionManagerLocal extends EcConnectionManagerNet{

	private static final long serialVersionUID = -9212054663979899431L;

	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {}
	
	@Override
	public void publish(CompoundEvent event) {}

}
