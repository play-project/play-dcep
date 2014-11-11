package eu.play_project.dcep.distributedetalis.listeners;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_ENTRY;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_ENTRY;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.dcep.node.api.EcConnectionmanagerException;
import eu.play_project.dcep.node.listeners.AbstractConnectionListenerRest;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import eu.play_project.play_eventadapter.NoRdfEventException;
import fr.inria.eventcloud.api.CompoundEvent;

@Singleton
public class EcConnectionListenerRest extends AbstractConnectionListenerRest<CompoundEvent> {

	private final AbstractReceiverRest rdfReceiver;
	private final Logger logger;

	public EcConnectionListenerRest() { // For JAXB
		this.rdfReceiver = null;
		this.logger = null;
	}
	
	public EcConnectionListenerRest(AbstractReceiverRest rdfReceiver) {
		super();
		this.rdfReceiver = rdfReceiver;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public Response notify(String stream, String notify) {
		if (this.getDcepNode() == null) {
			String msg = "Detalis was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		if (this.getDcepNode().getEcConnectionManager() == null) {
			String msg = "ecConnectionManager was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}

		String topic = Stream.toTopicUri(stream);
		
	    try {
	    	Model rdf = this.rdfReceiver.parseRdfRest(notify);
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
			    
			    // Store the event in Triple Store:
			    try {
					this.getDcepNode().getEcConnectionManager().putDataInCloud(event, topic);
				} catch (EcConnectionmanagerException e) {
					logger.warn("Could not persist event in historic triple store: {}: {}", e.getClass().getSimpleName(), e.getMessage());
				}
			}
			else {
				logger.info(LOG_DCEP_FAILED_ENTRY + "Duplicate Event suppressed: " + eventId);
			}

	    } catch (NoRdfEventException e) {
			logger.error("Received a non-RDF event on topic {} from the DSB: {}", topic, e.getMessage());
		}
	    
	    return Response.status(Status.ACCEPTED).build();
	}

}
