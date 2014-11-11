package eu.play_project.dcep.node.api;

import eu.play_project.dcep.api.SimplePublishApi;
import eu.play_project.play_platformservices.api.BdplQuery;

public interface EcConnectionManager<EventType> extends SimplePublishApi<EventType> {

	public SelectResults getDataFromCloud(String query,
			String cloudId) throws EcConnectionmanagerException;

	public void registerEventPattern(BdplQuery bdplQuery) throws EcConnectionmanagerException;

	public void unregisterEventPattern(BdplQuery bdplQuery);

	public void destroy();

	public void putDataInCloud(EventType event, String topic) throws EcConnectionmanagerException;

}