package eu.play_project.dcep.distributedetalis;

import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;

public class LocalEcConnectionManager extends EcConnectionManagerNet{
	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery){}
	@Override
	public void publish(CompoundEvent event) {}

}
