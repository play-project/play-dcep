package eu.play_project.dcep.node.listeners;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.ow2.play.governance.platform.user.api.rest.PublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.node.api.DcepNodeApi;

@Singleton
public abstract class AbstractConnectionListenerRest<EventType> extends Application implements PublishService, DuplicateCheckingListener<EventType> {

	private DcepNodeApi<EventType> dEtalis;
	private final Logger logger;
	/** Maintain a circular buffer of recent event IDs which have been seen to detect duplicate events arriving. */
	private final Collection<String> duplicatesCache =  Collections.synchronizedCollection(new CircularFifoQueue<String>(32));

	public AbstractConnectionListenerRest() {
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public void setDcepNode(DcepNodeApi<EventType> dEtalis) {
		this.dEtalis = dEtalis;
	}

	@Override
	public DcepNodeApi<EventType> getDcepNode() {
		return this.dEtalis;
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
