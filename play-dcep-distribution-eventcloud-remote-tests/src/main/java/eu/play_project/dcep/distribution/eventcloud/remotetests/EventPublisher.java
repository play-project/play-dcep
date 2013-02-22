package eu.play_project.dcep.distribution.eventcloud.remotetests;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;

import java.util.Calendar;

import org.event_processing.events.types.UcTelcoCall;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import eu.play_project.dcep.distributedetalis.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;

/**
 * Connect to Event Cloud and publish events.
 * 
 * @author Stefan Obermeier
 * 
 */
public class EventPublisher {
	PublishApi publishProxy = null;
	String eventCloudRegistryUrl = "pnp://eventcloud.inria.fr:8081/eventclouds-registry";

	public void publish() throws EventCloudIdNotManaged {
		if (publishProxy == null) {
			publishProxy = getOutputCloud("");
		}

		for (int i = 0; i < 100; i++) {
			publishProxy.publish(createTaxiUCCallEvent("http://example.com/" + Math.random()));
		}
	}

	private PublishApi getOutputCloud(String cloudId)
			throws EventCloudIdNotManaged {
		publishProxy = ProxyFactory.newPublishProxy(eventCloudRegistryUrl,
				new EventCloudId(cloudId));
		return publishProxy;
	}

	public static CompoundEvent createTaxiUCCallEvent(String eventId) {

		UcTelcoCall event = new UcTelcoCall(
		// set the RDF context part
				EventHelpers.createEmptyModel(eventId),
				// set the RDF subject
				eventId + EVENT_ID_SUFFIX,
				// automatically write the rdf:type statement
				true);

		// Run some setters of the event
		event.setUcTelcoCalleePhoneNumber("49123456789");
		event.setUcTelcoCallerPhoneNumber("49123498765");
		event.setUcTelcoDirection("incoming");

		double longitude = 123;
		double latitude = 345;
		EventHelpers.setLocationToEvent(event, longitude, latitude);

		// Create a Calendar for the current date and time
		event.setEndTime(Calendar.getInstance());
		event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));

		// Push events.
		return EventCloudHelpers.toCompoundEvent(event);
	}

}