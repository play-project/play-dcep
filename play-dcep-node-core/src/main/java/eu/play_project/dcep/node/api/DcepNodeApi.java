package eu.play_project.dcep.node.api;

import eu.play_project.dcep.api.DcepListenerApi;
import eu.play_project.dcep.api.SimplePublishApi;

public interface DcepNodeApi<EventType> extends DcepListenerApi<EventType>, SimplePublishApi<EventType> {
	
	EcConnectionManager<EventType> getEcConnectionManager();
	
}
