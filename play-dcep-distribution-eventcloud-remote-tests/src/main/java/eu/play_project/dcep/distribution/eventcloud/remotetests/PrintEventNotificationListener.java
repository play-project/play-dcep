package eu.play_project.dcep.distribution.eventcloud.remotetests;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class PrintEventNotificationListener extends CompoundEventNotificationListener {
	
	private static final long serialVersionUID = 100L;
	@Override
	public void onNotification(SubscriptionId id, CompoundEvent solution) {
		System.out.println("\n New Event:");
		System.out.println(solution);
	}

}
