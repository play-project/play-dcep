package eu.play_project.dcep.distributedetalis.api;

import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;

public interface EcConnectionManager extends SimplePublishApi{

	public SelectResults getDataFromCloud(String query,
			String cloudId) throws EcConnectionmanagerException, MalformedSparqlQueryException;

	/**
	 * Reuses or initiates an event cloud proxy.
	 */
	public PutGetApi getHistoricCloud(String cloudId)
			throws EcConnectionmanagerException;

	/**
	 * Reuses or initiates an event cloud proxy.
	 */
	public SubscribeApi getInputCloud(String cloudId)
			throws EcConnectionmanagerException;

	/**
	 * Reuses or initiates an event cloud proxy.
	 */
	public PublishApi getOutputCloud(String cloudId)
			throws EcConnectionmanagerException;

	public void publish(CompoundEvent event);

	public void registerEventPattern(EpSparqlQuery epSparqlQuery);

	public void unregisterEventPattern(EpSparqlQuery epSparqlQuery);

	public void subscribe(String cloudId);

	public void unsubscribe(String cloudId, Subscription sub);

	public void destroy();

}