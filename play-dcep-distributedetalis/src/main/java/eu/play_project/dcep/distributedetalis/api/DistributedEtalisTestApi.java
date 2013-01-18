package eu.play_project.dcep.distributedetalis.api;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerNet;
import fr.inria.eventcloud.api.CompoundEvent;


public interface DistributedEtalisTestApi extends SimplePublishApi {
	
	public void attach(SimplePublishApi subscriber);
	public void detach(SimplePublishApi subscriber);
	//Publish events directly without a connection manager.
	public void publish(CompoundEvent event);
	// Set own implementation of EcConnectionManager. E.g. version without network connection.
	public void setEcConnectionManager(EcConnectionManager ecConnectionManager);

}
