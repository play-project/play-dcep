package eu.play_project.dcep.distribution.eventcloud.remotetests;

import java.io.InputStream;
import java.util.Collection;


import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;

public class PrintEventNotificationListener extends CompoundEventNotificationListener {
	
	private static final long serialVersionUID = 1L;
	public void onNotification(SubscriptionId id, CompoundEvent solution) {
		System.out.println("\n New Event:");
		System.out.println(solution);
	}

}
