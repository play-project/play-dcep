package eu.play_project.dcep.distributedetalis;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.util.ModelUtils;
import org.ow2.play.governance.platform.user.api.rest.PublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import eu.play_project.play_eventadapter.NoRdfEventException;
import fr.inria.eventcloud.api.CompoundEvent;

@Path("/") // overwrite the Path from PublishService
@Singleton
public class EcConnectionListenerRestVirtuoso extends Application implements PublishService {

	private DistributedEtalis dEtalis;
	private final AbstractReceiverRest rdfReceiver;
	private final Logger logger;

	public EcConnectionListenerRestVirtuoso() { // For JAXB
		this.rdfReceiver = null;
		this.logger = null;
	}
	
	public EcConnectionListenerRestVirtuoso(AbstractReceiverRest rdfReceiver) {
		this.rdfReceiver = rdfReceiver;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public Response notify(String stream, String notify) {
		if (this.dEtalis == null) {
			String msg = "Detalis was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		if (this.dEtalis.getEcConnectionManager() == null) {
			String msg = "ecConnectionManager was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		
		String topic = Stream.toTopicUri(stream);
		
	    try {
	    	Model rdf = this.rdfReceiver.parseRdfRest(notify);
	    	ModelUtils.deanonymize(rdf);
	    	CompoundEvent event = EventCloudHelpers.toCompoundEvent(rdf);
	    	logger.debug("Received event {} on topic {} from the DSB.", event.getGraph(), topic);
	    	
		    // Forward the event to Detalis:
		    this.dEtalis.publish(event);
		    
		    // Store the event in Virtuoso:
		    ((EcConnectionManagerVirtuoso)this.dEtalis.getEcConnectionManager()).putDataInCloud(event, topic);
		    
	    } catch (NoRdfEventException e) {
			logger.error("Received a non-RDF event from the DSB: " + e.getMessage());
		}
	    
	    return Response.status(Status.ACCEPTED).build();
	}

	public void setDetalis(DistributedEtalis dEtalis) {
		this.dEtalis = dEtalis;
	}
}
