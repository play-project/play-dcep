package eu.play_project.dcep.distributedetalis;

import java.io.Serializable;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class EcConnectionListener extends CompoundEventNotificationListener implements Serializable{

	private DistributedEtalis dEtalis = null;

	// For ProActive:
	public EcConnectionListener() {
	}
	
	private static final long serialVersionUID = 8630112375640830481L;

	@Override
	public void onNotification(SubscriptionId id, CompoundEvent event) {
		this.dEtalis.publish(event);
	}

	public void setDetalis(DistributedEtalis dEtalis) {
		this.dEtalis = dEtalis;
	}

}