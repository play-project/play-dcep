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

public class EcConnectionListenerNet extends CompoundEventNotificationListener
		implements Serializable {

	private static final long serialVersionUID = 8630112375640830481L;

	private Deque<CompoundEvent> queue;

	@Override
	public void onNotification(SubscriptionId id, CompoundEvent event) {
		queue.add(event);
		//notifyAll();
	}

	// For ProActive:
	public EcConnectionListenerNet(){}
	
	public EcConnectionListenerNet(DistributedEtalis dEtalis) {
		queue = new LinkedList<CompoundEvent>();
		//new Thread(new Publisher(dEtalis, queue)).start();
	}

	class Publisher implements Runnable {
		private DistributedEtalis dEtalis;
		private Deque<CompoundEvent> queue;

		public Publisher(DistributedEtalis dEtalis, Deque<CompoundEvent> queue) {
			this.dEtalis = dEtalis;
			this.queue = queue;
		}

		@Override
		public void run() {
			while (true) {
				if (!queue.isEmpty()) {
					dEtalis.publish(queue.pollFirst());
				} else {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

}