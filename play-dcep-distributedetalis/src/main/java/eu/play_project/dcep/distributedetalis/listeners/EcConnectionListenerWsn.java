package eu.play_project.dcep.distributedetalis.listeners;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_ENTRY;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_ENTRY;

import java.io.Serializable;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;

import eu.play_project.dcep.distributedetalis.DistributedEtalis;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.utils.DsbHelpers;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import eu.play_project.play_eventadapter.NoRdfEventException;
import fr.inria.eventcloud.api.CompoundEvent;

public class EcConnectionListenerWsn implements INotificationConsumer, DuplicateCheckingListener, Serializable {

	private static final long serialVersionUID = 100L;
	private DistributedEtalis dEtalis;
	private final AbstractReceiverRest rdfReceiver;
	private final Logger logger;
	/** Maintain a circular buffer of recent event IDs which have been seen to detect duplicate events arriving. */
	private final Buffer duplicatesCache =  BufferUtils.synchronizedBuffer(new CircularFifoBuffer(32));
	
	public EcConnectionListenerWsn(AbstractReceiverRest rdfReceiver) {
		this.rdfReceiver = rdfReceiver;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public void notify(Notify notify) throws WsnbException {
		if (this.dEtalis == null) {
			String msg = "Detalis was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		if (this.dEtalis.getEcConnectionManager() == null) {
			String msg = "ecConnectionManager was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		
    	String topic = DsbHelpers.topicToUri(notify.getNotificationMessage().get(0).getTopic());

    	try {
	    	Model rdf = this.rdfReceiver.parseRdf(notify);
	    	ModelUtils.deanonymize(rdf);
	    	CompoundEvent event = EventCloudHelpers.toCompoundEvent(rdf);
	    	String eventId = event.getGraph().toString();
	    	logger.debug("Received event {} on topic {} from the DSB.", eventId, topic);
	
			/*
			 * Do some checking for duplicates (memorizing a few recently seen
			 * events)
			 */
			if (!isDuplicate(eventId)) {
				// Do not remove this line, needed for logs. :stuehmer
				logger.info(LOG_DCEP_ENTRY + eventId);
				logger.debug(LOG_DCEP + "Simple Event:\n{}", event);
				
			    // Forward the event to Detalis:
			    this.dEtalis.publish(event);
			    
			    // Store the event in Virtuoso:
			    this.dEtalis.getEcConnectionManager().putDataInCloud(event, topic);
			}
			else {
				logger.info(LOG_DCEP_FAILED_ENTRY + "Duplicate Event suppressed: " + eventId);
			}

		    
	    } catch (NoRdfEventException e) {
			logger.error("Received a non-RDF event on topic {} from the DSB: {}", topic, e.getMessage());
		} catch (EcConnectionmanagerException e) {
			logger.error("Could not store event on topic {} for historic storage: {}", topic, e.getMessage());
		}
	}

	public void setDetalis(DistributedEtalis dEtalis) {
		this.dEtalis = dEtalis;
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