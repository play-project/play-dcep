package eu.play_project.dcep.distributedetalis;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class EcConnectionListenerNet extends CompoundEventNotificationListener implements Serializable {
	private static final long serialVersionUID = 8630112375640830481L;
	private Deque<CompoundEvent> queue;

	// For ProActive:
	public EcConnectionListenerNet(){}
	
	public EcConnectionListenerNet(LinkedList<CompoundEvent> queue) {
		this.queue = queue;
	}
	
	@Override
	public void onNotification(SubscriptionId id, CompoundEvent event) {
		synchronized(queue){
			queue.add(event);
			queue.notifyAll();
		}
	}
}