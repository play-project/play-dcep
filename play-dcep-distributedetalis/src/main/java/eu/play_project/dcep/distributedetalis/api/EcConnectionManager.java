package eu.play_project.dcep.distributedetalis.api;

import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;

public interface EcConnectionManager extends SimplePublishApi{

	public SelectResults getDataFromCloud(String query,
			String cloudId) throws EcConnectionmanagerException;

	@Override
	public void publish(CompoundEvent event);

	public void registerEventPattern(EpSparqlQuery epSparqlQuery);

	public void unregisterEventPattern(EpSparqlQuery epSparqlQuery);

	public void destroy();

}