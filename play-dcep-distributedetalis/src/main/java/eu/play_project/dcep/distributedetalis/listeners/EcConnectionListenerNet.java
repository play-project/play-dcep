package eu.play_project.dcep.distributedetalis.listeners;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class EcConnectionListenerNet extends CompoundEventNotificationListener implements Serializable {
	private static final long serialVersionUID = 100L;
	private BlockingQueue<CompoundEvent> eventInputQueue;

	// For ProActive:
	public EcConnectionListenerNet(){}

	public EcConnectionListenerNet(BlockingQueue<CompoundEvent> eventInputQueue) {
		this.eventInputQueue = eventInputQueue;
	}

	@Override
	public void onNotification(SubscriptionId id, CompoundEvent event) {
		while (true) {
			try {
				eventInputQueue.put(event);
			} catch (InterruptedException e) {
			}
		}
	}
}