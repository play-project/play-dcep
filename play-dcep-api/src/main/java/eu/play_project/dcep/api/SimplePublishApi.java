package eu.play_project.dcep.api;


public interface SimplePublishApi<EventType> {

	public void publish(EventType event);
	
}
