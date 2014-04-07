package eu.play_project.dcep.api;

import fr.inria.eventcloud.api.CompoundEvent;


public interface DcepTestApi extends SimplePublishApi {
	
	public void attach(SimplePublishApi subscriber);
	public void detach(SimplePublishApi subscriber);
	//Publish events directly without a connection manager.
	@Override
	public void publish(CompoundEvent event);
}
