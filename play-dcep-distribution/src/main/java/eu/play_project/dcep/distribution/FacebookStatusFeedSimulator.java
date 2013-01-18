package eu.play_project.dcep.distribution;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static eu.play_project.play_commons.constants.Namespace.EVENTS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.event_processing.events.types.Event;
import org.event_processing.events.types.FacebookCepResult;
import org.event_processing.events.types.FacebookStatusFeedEvent;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;

public class FacebookStatusFeedSimulator {

	private static Random random = new Random();

	public List<Event> simulateEvents(String type, int number) {

		List<Event> events = new ArrayList<Event>();

		if (type.equals("FacebookSimple")) {

			FacebookStatusFeedEvent event;

			for (int i = number; i > 0; i--) {
				// Generate new event
				events.add(this.generateFacebookStatusFeedEvent());
			}
		} else if (type.equals("FacebookCepResult")) {

			for (int i = number; i > 0; i--) {

				events.add(this.generateFacebookCepResult());
			}
		}

		return events;
	}

	private FacebookStatusFeedEvent generateFacebookStatusFeedEvent() {
		// Create an event ID used in RDF context and RDF subject
		String eventId = EVENTS.getUri() + "e" + Math.abs(random.nextLong());

		FacebookStatusFeedEvent event = new FacebookStatusFeedEvent(
		// set the RDF context part
				EventHelpers.createEmptyModel(eventId),
				// set the RDF subject
				eventId + EVENT_ID_SUFFIX,
				// automatically write the rdf:type statement
				true);

		// Run some setters of the event
		event.setFacebookName("Roland Stühmer");
		event.setFacebookId("100000058455726");
		event.setFacebookLink(new URIImpl("http://graph.facebook.com/roland.stuehmer#"));
		event.setStatus("I bought some JEANS this morning");
		event.setFacebookLocation("Karlsruhe, Germany");
		// Create a Calendar for the current date and time
		event.setEndTime(Calendar.getInstance());
		event.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));

		return event;
	}

	private FacebookCepResult generateFacebookCepResult() {
		// Create an event ID used in RDF context and RDF subject
		String eventId = EVENTS.getUri() + "e" + Math.abs(random.nextLong());

		FacebookCepResult event = new FacebookCepResult(
				// set the RDF context part
				EventHelpers.createEmptyModel(eventId),
				// set the RDF subject
				eventId + EVENT_ID_SUFFIX,
				// automatically write the rdf:type statement
				true);

		// Run some setters of the event
		event.addDiscussionTopic("I bought some JEANS this morning");
		event.addDiscussionTopic("I never bought JEANS");
		event.addDiscussionTopic("I plan to buy JEANS today...");
		event.addFriend("Laurent Pellegrino");
		event.addFriend("Stefan Obermeier");
		event.addFriend("Roland St\u00FChmer");
		// Create a Calendar for the current date and time
		event.setEndTime(Calendar.getInstance());
		event.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));

		return event;
	}
}
