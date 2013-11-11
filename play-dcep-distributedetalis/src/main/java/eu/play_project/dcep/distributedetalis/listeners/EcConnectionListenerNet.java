package eu.play_project.dcep.distributedetalis.listeners;

import java.io.Serializable;
import java.util.Queue;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class EcConnectionListenerNet extends CompoundEventNotificationListener implements Serializable {
	private static final long serialVersionUID = 100L;
	private Queue<CompoundEvent> eventInputQueue;

	// For ProActive:
	public EcConnectionListenerNet(){}

	public EcConnectionListenerNet(Queue<CompoundEvent> eventInputQueue2) {
		this.eventInputQueue = eventInputQueue2;
	}

	@Override
	public void onNotification(SubscriptionId id, CompoundEvent event) {
		eventInputQueue.add(event);
		eventInputQueue.notifyAll();
	}
}