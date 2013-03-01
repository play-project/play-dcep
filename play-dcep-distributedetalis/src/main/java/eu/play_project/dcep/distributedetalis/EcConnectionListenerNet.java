package eu.play_project.dcep.distributedetalis;

import java.io.Serializable;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class EcConnectionListenerNet extends CompoundEventNotificationListener implements Serializable {
	private static final long serialVersionUID = 8630112375640830481L;

	// For ProActive:
	public EcConnectionListenerNet(){}

	@Override
	public void onNotification(SubscriptionId id, CompoundEvent event) {
		synchronized(EcConnectionManagerNet.eventInputQueue){
			EcConnectionManagerNet.eventInputQueue.add(event);
			EcConnectionManagerNet.eventInputQueue.notifyAll();
		}	
	}
}