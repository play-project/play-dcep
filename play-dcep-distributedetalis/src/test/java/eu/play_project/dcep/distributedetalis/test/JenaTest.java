package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ontoware.rdf2go.model.ModelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;

import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

public class JenaTest {
	
	private final Logger logger = LoggerFactory.getLogger(JenaTest.class);
	
	/**
	 * Write and read events from Jena triplestore.
	 */
	@Test
	public void testTripleQuadrupleStorrage() {
		
		// Generate events.
		List<Quadruple> quadruple = new ArrayList<Quadruple>();
		quadruple
		.add(new Quadruple(
				NodeFactory.createURI("http://eventId1_1"),
				NodeFactory.createURI("http://events.event-processing.org/ids/e2#event"),
				NodeFactory.createURI("http://events.event-processing.org/ids/endTime"),
				NodeFactory.createURI("1234")));
		
		List<Quadruple> quadruple2 = new ArrayList<Quadruple>();
		quadruple2
		.add(new Quadruple(
				NodeFactory.createURI("http://eventId1_2"),
				NodeFactory.createURI("http://events.event-processing.org/ids/e2#event"),
				NodeFactory.createURI("http://events.event-processing.org/ids/endTime"),
				NodeFactory.createURI("1234")));
		
		
		CompoundEvent event1 = new CompoundEvent(quadruple);
		CompoundEvent event2 = new CompoundEvent(quadruple2);
		
		// Put events in triplestore.
		ModelSet rdf = EventHelpers.createEmptyModelSet();
		rdf.addModel(EventCloudHelpers.toRdf2go(event1));
		rdf.addModel(EventCloudHelpers.toRdf2go(event2));
		
		// Query data from model
		String query = "SELECT ?id "
						+ "WHERE{ "
						+ " GRAPH ?id {?s ?p ?o}"
						+ "}";
		
		Query jenaQuery = QueryFactory.create(query, Syntax.syntaxSPARQL_11);

		Dataset jena;
		try {
			jena = (Dataset) rdf.getUnderlyingModelSetImplementation();
		} catch (QueryParseException e) {
			logger.error("Query with pars error: {}", query);
			throw e;
		}

		logger.debug("Execute historical query: {}", query);
		QueryExecution qexec = QueryExecutionFactory.create(jenaQuery, jena);

		ResultRegistry results = null;
		try {
			results = ResultRegistry.makeResult(new ResultSetWrapper(qexec.execSelect()));
		} finally {
			qexec.close();
		}
		
		
		// Check results.
		assertEquals("http://eventId1_1", results.getResult().get(1).get(0).toString());
		assertEquals("http://eventId1_2", results.getResult().get(0).get(0).toString());
	}

}
