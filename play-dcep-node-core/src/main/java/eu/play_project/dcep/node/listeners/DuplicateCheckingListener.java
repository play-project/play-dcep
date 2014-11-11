package eu.play_project.dcep.node.listeners;

import eu.play_project.dcep.node.api.DcepNodeApi;

public interface DuplicateCheckingListener<EventType> {

	public boolean isDuplicate(String eventId);
	public void setDcepNode(DcepNodeApi<EventType> dEtalis);
	public DcepNodeApi<EventType> getDcepNode();

}
