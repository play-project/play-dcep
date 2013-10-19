package eu.play_project.dcep.distributedetalis.test;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static eu.play_project.play_commons.constants.Namespace.EVENTS;
import static eu.play_project.play_commons.constants.Namespace.TYPES;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.event_processing.events.types.Event;
import org.junit.Test;
import org.ontoware.rdf2go.impl.jena.TypeConversion;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class EventCloudHelpersTest {

	private final String eventId = EVENTS.getUri() + "1234";
	private final Node GRAPHNAME = NodeFactory.createURI(eventId);
	// Subjects
	private final Node SUBJECT = NodeFactory.createURI(eventId + EVENT_ID_SUFFIX);
	private final Node OTHER_SUBJECT = NodeFactory.createURI(eventId + "bogus");
	
	// Predicates
	private final Node STREAM = TypeConversion.toJenaNode(Event.STREAM);
	
	// Objects
	private final Node EVENT_TYPE_1 = NodeFactory.createURI(TYPES.getUri() + "Type1");
	private final Node EVENT_TYPE_2 = NodeFactory.createURI(TYPES.getUri() + "Type2");
	private final Node EVENT_TYPE_DEFAULT = TypeConversion.toJenaNode(Event.RDFS_CLASS);

	/**
	 * Test an event with proper subject.
	 */
	@Test
	public void testGetEventTypeWithSubject() {
		List<Quadruple> quadruple = new ArrayList<Quadruple>();

		quadruple.add(new Quadruple(
				GRAPHNAME,
				SUBJECT,
				RDF.type.asNode(),
				EVENT_TYPE_1));

		quadruple.add(new Quadruple(
				GRAPHNAME,
				OTHER_SUBJECT,
				RDF.type.asNode(),
				EVENT_TYPE_2));

		CompoundEvent event = new CompoundEvent(quadruple);
		
		assertEquals(EVENT_TYPE_1.toString(), EventCloudHelpers.getEventType(event));
	}
	
	/**
	 * Test an event with no proper subject.
	 */
	@Test
	public void testGetEventTypeWithoutSubject() {
		List<Quadruple> quadruple = new ArrayList<Quadruple>();

		quadruple.add(new Quadruple(
				GRAPHNAME,
				OTHER_SUBJECT,
				RDF.type.asNode(),
				EVENT_TYPE_2));

		CompoundEvent event = new CompoundEvent(quadruple);
		
		assertEquals(EVENT_TYPE_2.toString(), EventCloudHelpers.getEventType(event));
	}

	/**
	 * Test an event with no type declaration whatsoever: the default event type
	 * should be returned.
	 */
	@Test
	public void testGetEventTypeWithoutType() {
		List<Quadruple> quadruple = new ArrayList<Quadruple>();

		quadruple.add(new Quadruple(
				GRAPHNAME,
				OTHER_SUBJECT,
				RDF.first.asNode(), // arbitrary predicate
				EVENT_TYPE_2));

		CompoundEvent event = new CompoundEvent(quadruple);
		
		assertEquals(EVENT_TYPE_DEFAULT.toString(), EventCloudHelpers.getEventType(event));
	}
	
	/**
	 * Test finding the correct cloud ID from an event.
	 */
	@Test
	public void testGetCloudId() {
		List<Quadruple> quadruple;
		CompoundEvent event;
		
		// check proper graph, subject and predicate
		quadruple = new ArrayList<Quadruple>();
		quadruple.add(new Quadruple(
				GRAPHNAME,
				SUBJECT,
				STREAM,
				NodeFactory.createURI(Stream.ActivityEventStream.getUri())));
		event = new CompoundEvent(quadruple);
		assertEquals(Stream.ActivityEventStream.getTopicUri(), EventCloudHelpers.getCloudId(event));
		
		// check proper graph, subject and predicate with manually created URIs
		quadruple = new ArrayList<Quadruple>();
		quadruple.add(new Quadruple(
				GRAPHNAME,
				SUBJECT,
				NodeFactory.createURI("http://events.event-processing.org/types/stream"),
				NodeFactory.createURI("http://streams.event-processing.org/ids/TaxiUCESRRecomDcep#stream")));
		event = new CompoundEvent(quadruple);
		assertEquals(NodeFactory.createURI("http://streams.event-processing.org/ids/TaxiUCESRRecomDcep").toString(), EventCloudHelpers.getCloudId(event));

		// check proper graph, predicate an improper subject
		quadruple = new ArrayList<Quadruple>();
		quadruple.add(new Quadruple(
				GRAPHNAME,
				OTHER_SUBJECT,
				STREAM,
				NodeFactory.createURI(Stream.ActivityEventStream.getUri())));
		event = new CompoundEvent(quadruple);
		assertEquals(Stream.ActivityEventStream.getTopicUri(), EventCloudHelpers.getCloudId(event));

		// check if proper subject overrules improper subject (primary choice)
		quadruple = new ArrayList<Quadruple>();
		quadruple.add(new Quadruple(
				GRAPHNAME,
				OTHER_SUBJECT,
				STREAM,
				NodeFactory.createURI(Stream.TwitterFeed.getUri())));
		quadruple.add(new Quadruple(
				GRAPHNAME,
				SUBJECT,
				STREAM,
				NodeFactory.createURI(Stream.ActivityEventStream.getUri())));
		event = new CompoundEvent(quadruple);
		assertEquals(Stream.ActivityEventStream.getTopicUri(), EventCloudHelpers.getCloudId(event));

		// check improper predicate (no stream can be found, empty string is returned)
		quadruple = new ArrayList<Quadruple>();
		quadruple.add(new Quadruple(
				GRAPHNAME,
				SUBJECT,
				NodeFactory.createURI("http://domain.invalid/bogus_uri"),
				NodeFactory.createURI(Stream.ActivityEventStream.getUri())));
		event = new CompoundEvent(quadruple);
		assertEquals("", EventCloudHelpers.getCloudId(event));

	}
}
