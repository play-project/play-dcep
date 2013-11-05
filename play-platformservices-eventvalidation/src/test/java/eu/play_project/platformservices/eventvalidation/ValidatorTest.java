package eu.play_project.platformservices.eventvalidation;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.event_processing.events.types.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.DatatypeLiteralImpl;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdf2go.vocabulary.XSD;
import org.ontoware.rdfreactor.schema.rdfs.Resource;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Test cases associated to {@link Validator}.
 * 
 * @author lpellegr
 */
public class ValidatorTest {

	private static final Node ENDTIME_NODE =
			NodeFactory.createURI(Event.ENDTIME.toString());

	private static final Node SOURCE_NODE =
			NodeFactory.createURI(Event.SOURCE.toString());

	private static final Node STREAM_NODE =
			NodeFactory.createURI(Event.STREAM.toString());

	private static final Node TYPE_NODE =
			NodeFactory.createURI(Event.TYPE.toString());

	private static final Node GRAPH_NODE =
			NodeFactory.createURI("http://events.event-processing.org/ids/ava-13");

	private static final Node SUBJECT_NODE =
			NodeFactory.createURI(GRAPH_NODE + EVENT_ID_SUFFIX);

	private static final URI GRAPH_URI =
			new URIImpl("http://events.event-processing.org/ids/ava-13");
	
	private static final URI SUBJECT_URI =
			new URIImpl(GRAPH_URI + EVENT_ID_SUFFIX);
	
	private Validator validator;

	@Before
	public void setUp() {
		this.validator = new Validator();
	}

	@Test
	public void testValidationWithJenaObjects() {
		/*
		 * Build the event until it is valid:
		 */
		Quad q = new Quad(GRAPH_NODE, SUBJECT_NODE, ENDTIME_NODE,
				NodeFactory.createLiteral("2011-12-06T18:33:36.681", XSDDatatype.XSDdateTime));
		this.validator.checkQuadruple(q);
		try {
			assertFalse(this.validator.isValid());
			fail("This should not be reached.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}
		
		q = new Quad(
				GRAPH_NODE,
				SUBJECT_NODE,
				STREAM_NODE,
				NodeFactory.createURI("http://sources.event-processing.org/ids/topicName#stream"));
		this.validator.checkQuadruple(q);
		try {
			assertFalse(this.validator.isValid());
			fail("This should not be reached.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}
		
		q = new Quad(
				GRAPH_NODE,
				SUBJECT_NODE,
				SOURCE_NODE,
				NodeFactory.createURI("http://sources.event-processing.org/ids/box12#source"));
		this.validator.checkQuadruple(q);
		try {
			assertFalse(this.validator.isValid());
			fail("This should not be reached.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}
		
		q = new Quad(
				GRAPH_NODE,
				SUBJECT_NODE,
				TYPE_NODE,
				NodeFactory.createURI("http://events.event-processing.org/types/DummyType"));
		this.validator.checkQuadruple(q);
		try {
			assertTrue(this.validator.isValid());
		} catch (InvalidEventException e) {
			fail("This should not be reached.");
		}

		q = new Quad(GRAPH_NODE,
				SUBJECT_NODE,
				NodeFactory.createURI("http://example.org/property/x"),
				NodeFactory.createLiteral("1", XSDDatatype.XSDint));
		this.validator.checkQuadruple(q);
		try {
			assertTrue(this.validator.isValid());
		} catch (InvalidEventException e) {
			fail("This should not be reached.");
		}
		
		/*
		 * Now make it invalid:
		 */
		q = new Quad(GRAPH_NODE,
				SUBJECT_NODE,
				NodeFactory.createURI("http://example.org/property/y"),
				NodeFactory.createAnon());
		this.validator.checkQuadruple(q);
		try {
			this.validator.isValid();
			fail("Adding a blank node should fail this test.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void testValidationWithRdf2goObjects() {
		/*
		 * Build the event until it is valid:
		 */
		this.validator.checkQuadruple(
				GRAPH_URI, SUBJECT_URI, Event.ENDTIME,
				new DatatypeLiteralImpl("2011-12-06T18:33:36.681", XSD._dateTime));
		try {
			assertFalse(this.validator.isValid());
			fail("This should not be reached.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}

		this.validator.checkQuadruple(
				GRAPH_URI, SUBJECT_URI, Event.STREAM,
				new URIImpl("http://sources.event-processing.org/ids/topicName#stream"));
		try {
			this.validator.isValid();
			fail("This should not be reached.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}
		
		this.validator.checkQuadruple(
				GRAPH_URI, SUBJECT_URI, Event.SOURCE,
				new URIImpl("http://sources.event-processing.org/ids/box12#source"));
		try {
			this.validator.isValid();
			fail("This should not be reached.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}
		
		this.validator.checkQuadruple(
				GRAPH_URI, SUBJECT_URI, Resource.TYPE,
				new URIImpl("http://events.event-processing.org/types/DummyType"));
		try {
			assertTrue(this.validator.isValid());
		} catch (InvalidEventException e) {
			fail("This should not be reached.");
		}
		
		this.validator.checkQuadruple(
				GRAPH_URI, SUBJECT_URI, new URIImpl("http://example.org/property/x"),
				new DatatypeLiteralImpl("1", XSD._int));
		try {
			assertTrue(this.validator.isValid());
		} catch (InvalidEventException e) {
			fail("This should not be reached.");
		}
		
		/*
		 * Now make it invalid:
		 */
		this.validator.checkQuadruple(
				GRAPH_URI, SUBJECT_URI, Event.SOURCE,
				RDF2Go.getModelFactory().createModel().createBlankNode());
		try {
			assertFalse(this.validator.isValid());
			fail("Adding a blank node should fail this test.");
		} catch (InvalidEventException e) {
			System.out.println(e.getMessage());
		}
	}

	@After
	public void tearDown() {
		this.validator = null;
	}

}
