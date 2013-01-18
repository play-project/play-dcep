package eu.play_project.querydispatcher.epsparql.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.StreamIdCollector;

public class StreamIdCollectorTest {

	@Test
	public void testStreamIdCollector() {
		
		String[] expectedInputStreams = {Stream.TwitterFeed.getTopicUri(), Stream.TaxiUCGeoLocation.getTopicUri(), Stream.TaxiUCGeoLocation.getTopicUri()};
		String expectedOutputStream = Stream.ContextualizedLatitudeFeed.getTopicUri();
		
		// Get query.
		String queryString = EpSparqlEle01Test.getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
		String queryId = "urn:bdpl:exampleQuery";
		
		assertNotNull("Testing Query was not found on classpath", queryString);
		
		Query q = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
		
		QueryDetails qd = new QueryDetails();
		qd.setQueryId(queryId);

		qd.setWindowTime(q.getWindowTime());

		StreamIdCollector streamIdCollector = new StreamIdCollector();
		streamIdCollector.getStreamIds(q, qd);

		// Test output stream
		assertTrue(qd.getOutputStream().equals(expectedOutputStream));
		
		// Test input streams
		for (int i = 0; i < qd.getInputStreams().size(); i++) {
			assertTrue(qd.getInputStreams().get(i).equals(expectedInputStreams[i]));
		}

		
	}
}
