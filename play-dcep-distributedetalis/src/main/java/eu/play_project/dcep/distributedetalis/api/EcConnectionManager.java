package eu.play_project.dcep.distributedetalis.api;

import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_platformservices.api.BdplQuery;
import fr.inria.eventcloud.api.CompoundEvent;

public interface EcConnectionManager extends SimplePublishApi{

	public SelectResults getDataFromCloud(String query,
			String cloudId) throws EcConnectionmanagerException;

	@Override
	public void publish(CompoundEvent event);

	public void registerEventPattern(BdplQuery bdplQuery) throws EcConnectionmanagerException;

	public void unregisterEventPattern(BdplQuery bdplQuery);

	public void destroy();

	public void putDataInCloud(CompoundEvent event, String topic) throws EcConnectionmanagerException;;

}