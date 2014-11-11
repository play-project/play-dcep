package eu.play_project.dcep.node.listeners;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;

import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.play_eventadapter.AbstractReceiverRest;

public abstract class AbstractConnectionListenerWsn<EventType> implements INotificationConsumer, DuplicateCheckingListener<EventType>, Serializable {

	private static final long serialVersionUID = 100L;
	private DcepNodeApi<EventType> dEtalis;
	private final Logger logger;
	private final AbstractReceiverRest rdfReceiver;

	/** Maintain a circular buffer of recent event IDs which have been seen to detect duplicate events arriving. */
	private final Collection<String> duplicatesCache =  Collections.synchronizedCollection(new CircularFifoQueue<String>(32));
	
	public AbstractConnectionListenerWsn(AbstractReceiverRest rdfReceiver) {
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.rdfReceiver = rdfReceiver;
	}
	
	@Override
	public void setDcepNode(DcepNodeApi<EventType> dEtalis) {
		this.dEtalis = dEtalis;
	}

	@Override
	public DcepNodeApi<EventType> getDcepNode() {
		return this.dEtalis;
	}
	
	public AbstractReceiverRest getRdfReceiver() {
		return this.rdfReceiver;
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
}