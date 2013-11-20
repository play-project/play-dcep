package eu.play_project.dcep.distributedetalis.listeners;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_ENTRY;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_ENTRY;

import java.io.Serializable;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerNet;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class EcConnectionListenerNet extends CompoundEventNotificationListener implements Serializable, DuplicateCheckingListener {
	private static final long serialVersionUID = 100L;
	/** Maintain a circular buffer of recent event IDs which have been seen to detect duplicate events arriving. */
	private final Buffer duplicatesCache =  BufferUtils.synchronizedBuffer(new CircularFifoBuffer(32));
	private final Logger logger = LoggerFactory.getLogger(EcConnectionListenerNet.class);

	// For ProActive:
	public EcConnectionListenerNet(){}

	@Override
	public void onNotification(SubscriptionId id, CompoundEvent event) {
    	String eventId = event.getGraph().toString();

		/*
		 * Do some checking for duplicates (memorizing a few recently seen
		 * events)
		 */
		if (!isDuplicate(eventId)) {
			// Do not remove this line, needed for logs. :stuehmer
			logger.info(LOG_DCEP_ENTRY + eventId);
			logger.debug(LOG_DCEP + "Simple Event:\n{}", event);
			
		    // Forward the event to Detalis:
			synchronized(EcConnectionManagerNet.eventInputQueue){
				EcConnectionManagerNet.eventInputQueue.add(event);
				EcConnectionManagerNet.eventInputQueue.notifyAll();
			}
		}
		else {
			logger.info(LOG_DCEP_FAILED_ENTRY + "Duplicate Event suppressed: " + eventId);
		}

		
	}

	@SuppressWarnings("unchecked") // TODO stuehmer: will be fixed with https://github.com/play-project/play-dcep/issues/15
	@Override
	public boolean isDuplicate(String eventId) {
		if (duplicatesCache.contains(eventId)) {
			return true;
		}
		else {
			duplicatesCache.add(eventId);
			return false;
		}
	}
}