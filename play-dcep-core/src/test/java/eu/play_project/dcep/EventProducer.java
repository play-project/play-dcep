package eu.play_project.dcep;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import org.event_processing.events.types.FacebookStatusFeedEvent;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;

public class EventProducer implements Serializable{

	private static final long serialVersionUID = 2179301964522742234L;
	PublishApi dcepPublishApi;
	ArrayList<CompoundEvent> events = null;


	public void setDestination(PublishApi dcepPublishApi){
		this.dcepPublishApi = dcepPublishApi;
	}
	
	public void sendEvents(int number){
		//System.out.println("Send " + number + "new Events");
		if(events==null){
			events =new ArrayList<CompoundEvent>();
			for(int i=0; i<number; i++){
				events.add(createEvent(i + ""));
				dcepPublishApi.publish(events.get(i));
			}
		} else {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// System.out.println("Start sending" + number + "events");
			for (int i = 0; i < number; i++) {

				dcepPublishApi.publish(events.get(i));
			}
		}

	}

	public static CompoundEvent  createEvent(String eventId){

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
		EventHelpers.setLocationToEvent(event, 6, 7);
		
		//Push events.
		return EventCloudHelpers.toCompoundEvent(event);
	}
}
