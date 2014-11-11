package eu.play_project.dcep.distributedetalis.listeners;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_ENTRY;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_ENTRY;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.dcep.node.listeners.DuplicateCheckingListener;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class EcConnectionListenerNet extends CompoundEventNotificationListener implements Serializable, DuplicateCheckingListener<CompoundEvent> {
	private static final long serialVersionUID = 100L;
	private BlockingQueue<CompoundEvent> eventInputQueue;
	/** Maintain a circular buffer of recent event IDs which have been seen to detect duplicate events arriving. */
	private final Collection<String> duplicatesCache =  Collections.synchronizedCollection(new CircularFifoQueue<String>(32));
	private final Logger logger = LoggerFactory.getLogger(EcConnectionListenerNet.class);

	// For ProActive:
	public EcConnectionListenerNet(){}

	public EcConnectionListenerNet(BlockingQueue<CompoundEvent> eventInputQueue) {
		this.eventInputQueue = eventInputQueue;
	}

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
			while (true) {
				try {
					eventInputQueue.put(event);
					break;
				} catch (InterruptedException e) {
					logger.debug("Thread '{}' got interrupted while waiting to put an event in the queue.", Thread.currentThread());
					// try again storing the same event in the loop
				}
			}
		}
		else {
			logger.info(LOG_DCEP_FAILED_ENTRY + "Duplicate Event suppressed: " + eventId);
		}
	}

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

	@Override
	public void setDcepNode(DcepNodeApi<CompoundEvent> dEtalis) {
	}

	@Override
	public DcepNodeApi<CompoundEvent> getDcepNode() {
		return null;
	}
}