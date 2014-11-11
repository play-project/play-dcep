package eu.play_project.dcep.distributedetalis.listeners;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_ENTRY;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_ENTRY;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;

import eu.play_project.dcep.distributedetalis.utils.DsbHelpers;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.dcep.node.api.EcConnectionmanagerException;
import eu.play_project.dcep.node.listeners.AbstractConnectionListenerWsn;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import eu.play_project.play_eventadapter.NoRdfEventException;
import fr.inria.eventcloud.api.CompoundEvent;

public class EcConnectionListenerWsn extends AbstractConnectionListenerWsn<CompoundEvent> {

	private static final long serialVersionUID = 100L;
	private final Logger logger;
	
	public EcConnectionListenerWsn(AbstractReceiverRest rdfReceiver) {
		super(rdfReceiver);
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public void notify(Notify notify) throws WsnbException {
		if (this.getDcepNode() == null) {
			String msg = "Detalis was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		if (this.getDcepNode().getEcConnectionManager() == null) {
			String msg = "ecConnectionManager was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		
    	String topic = DsbHelpers.topicToUri(notify.getNotificationMessage().get(0).getTopic());

    	try {
	    	Model rdf = this.getRdfReceiver().parseRdf(notify);
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
			    this.getDcepNode().publish(event);
			    
			    // Store the event in Virtuoso:
			    this.getDcepNode().getEcConnectionManager().putDataInCloud(event, topic);
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

}