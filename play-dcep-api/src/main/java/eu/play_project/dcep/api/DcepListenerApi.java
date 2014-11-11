package eu.play_project.dcep.api;

public interface DcepListenerApi<EventType> {

	public void attach(SimplePublishApi<EventType> subscriber);
	public void detach(SimplePublishApi<EventType> subscriber);

}
