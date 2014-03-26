package eu.play_project.querydispatcher.bdpl.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.CountEventsVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventMembersFromStream;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;
/**
 * 
 * @author sobermeier
 *
 */
public class DispatcherTest {
	private static Logger logger;

	@Test
	public void testGetIoStreamIds() throws IOException{
		logger = LoggerFactory.getLogger(DispatcherTest.class);
		String queryString;
		Set<String> expectedInputStreams = new HashSet<String>(Arrays.asList(new String[] {"http://streams.event-processing.org/ids/TwitterFeed", "http://streams.event-processing.org/ids/TaxiUCGeoLocation", "http://streams.event-processing.org/ids/TaxiUCGeoLocation"}));
		String expectedOutputStream = "http://streams.event-processing.org/ids/ContextualizedLatitudeFeed";
		
		// Get query.
		queryString = BdplEleTest.getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		StreamIdCollector streamIdCollector = new StreamIdCollector();
	
		QueryDetails qd = new QueryDetails();
		streamIdCollector.getStreamIds(query, qd);
		
		// Test toString() implementation
		assertTrue(qd.toString().contains(expectedOutputStream));

		// Test output stream
		assertEquals(expectedOutputStream, qd.getOutputStream());
		
		// Test input streams
		assertEquals(expectedInputStreams, qd.getInputStreams());

	}
	
	@Test
	public void testGetVariablesAndTypes() throws IOException{
		if(logger == null){
			logger= LoggerFactory.getLogger(DispatcherTest.class);
		}
		
		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("queries/play-bdpl-clic2Call.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		
		VariableTypeManager vtm = new VariableTypeManager(query);
		
		vtm.collectVars();
		System.out.println(queryString);
		List<String> vars = vtm.getVariables(VariableTypes.CONSTRUCT_TYPE);
		assertTrue(vars.size() == 6);
		assertTrue(vars.contains("e1"));
		assertTrue(vars.contains("e2"));
		assertTrue(vars.contains("bob"));
		assertTrue(vars.contains("alice"));
		assertTrue(vars.contains("tweetContent"));
		assertTrue(vars.contains("id1"));

		vars = vtm.getVariables(VariableTypes.REALTIME_TYPE);
		assertTrue(vars.size() == 8);
		assertTrue(vars.contains("id1"));
		assertTrue(vars.contains("e1"));
		assertTrue(vars.contains("e2"));
		assertTrue(vars.contains("alice"));
		assertTrue(vars.contains("bob"));
		assertTrue(vars.contains("direction"));
		assertTrue(vars.contains("firstEvent"));
		assertTrue(vars.contains("id2"));


		vars = vtm.getVariables(VariableTypes.HISTORIC_TYPE);
		assertTrue(vars.size() == 5);
		assertTrue(vars.contains("id3"));
		assertTrue(vars.contains("e3"));
		assertTrue(vars.contains("tweetTime"));
		assertTrue(vars.contains("firstEvent"));
		assertTrue(vars.contains("tweetContent"));
	}
	
	@Test
	public void testDispatchQueryHistoricalMultipleClouds() throws IOException {
		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("queries/BDPL-Query-Realtime-Historical-multiple-Clouds.eprq");
		System.out.println(queryString);
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		// Dispatch query
		List<HistoricalQuery> queries = PlaySerializer.serializeToMultipleSelectQueries(query);

		// Check if result is a valid SPARQL string.
		try {
			QueryFactory.create(queries.get(0).getQuery(), com.hp.hpl.jena.query.Syntax.syntaxSPARQL_11);
			QueryFactory.create(queries.get(1).getQuery(), com.hp.hpl.jena.query.Syntax.syntaxSPARQL_11);
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void testQueryDispatchHistoricToken() throws IOException {
		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("queries/MultipleDestinationHistoric.eprq");
		System.out.println(queryString);
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		// Dispatch query
		List<HistoricalQuery> queries = PlaySerializer.serializeToMultipleSelectQueries(query);

		// Check if result is a valid SPARQL string.
		System.out.println(queries.get(0).getQuery());
		try {
			QueryFactory.create(queries.get(0).getQuery(), com.hp.hpl.jena.query.Syntax.syntaxSPARQL_11);
			QueryFactory.create(queries.get(1).getQuery(), com.hp.hpl.jena.query.Syntax.syntaxSPARQL_11);
		} catch (Exception e) {
			fail();
		}
	}
	

	@Test
	public void testQueryDispatchHistoric() throws IOException {
		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("queries/1-BidPhase_gps-region-detection.eprq");
		System.out.println(queryString);
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		// Dispatch query
		List<HistoricalQuery> queries = PlaySerializer.serializeToMultipleSelectQueries(query);

		// Check if result is a valid SPARQL string.
		try {
			QueryFactory.create(queries.get(0).getQuery(), com.hp.hpl.jena.query.Syntax.syntaxSPARQL_11);
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void testDispatchMissedCallsPlusTwitterQuery() throws IOException{
		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		
		StreamIdCollector streamIdCollector = new StreamIdCollector ();
		
		QueryDetails qd = new QueryDetails();
		streamIdCollector.getStreamIds(query, qd);

		assertEquals("http://streams.event-processing.org/ids/TaxiUCClic2Call", qd.getOutputStream());
		assertTrue(qd.getInputStreams().contains("http://streams.event-processing.org/ids/TaxiUCCall"));
	}
	
	@Test
	public void testQueryTemplateGenerator() throws IOException{
		 QueryTemplateGenerator templateGenerator  =  new  QueryTemplateGenerator();
		 
		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
			
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		
		QueryTemplate qt = templateGenerator.createQueryTemplate(query);
	}
	
	@Test
	public void generateBdplQuery() throws IOException, QueryDispatchException {
		EleGenerator eleGenerator =  new EleGeneratorForConstructQuery();
		String queryString = getSparqlQuery("play-epsparql-telco-recom-tweets-historic.eprq");
		
		// Parse query
		Query q;
		try {
			q = QueryFactory.create(queryString, Syntax.syntaxBDPL);
		} catch (com.hp.hpl.jena.query.QueryException e) {
			throw new QueryDispatchException(e.getMessage());
		}

		// Generate CEP-language
		eleGenerator.setPatternId("patternId1");
		eleGenerator.generateQuery(q);

		// Add queryDetails
		QueryDetails qd = new QueryDetails("patternId1");

		// Set properties for windows in QueryDetails
		ElementWindowVisitor windowVisitor = new WindowVisitor(qd);
		q.getWindow().accept(windowVisitor);

		// Set stream ids in QueryDetails.
		StreamIdCollector streamIdCollector = new StreamIdCollector();
		streamIdCollector.getStreamIds(q, qd);
		
		// Set complex event type.
		qd.setComplexType((new ComplexTypeFinder()).visit(q.getConstructTemplate()));

		qd.setRdfDbQueries(eleGenerator.getRdfDbQueries());
		
		BdplQuery bdpl = BdplQuery.builder()
				.details(qd)
				.ele(eleGenerator.getEle())
				.historicalQueries(PlaySerializer.serializeToMultipleSelectQueries(q))
				.constructTemplate(new QueryTemplateGenerator().createQueryTemplate(q))
				.bdpl(queryString)
				.build();
		
		assertTrue("Historical query is not marked as query with shared Variables.", bdpl.getHistoricalQueries().get(0).hasSharedVariablesWithRealtimePart());
		assertTrue(bdpl.getEleQuery().contains(",variabeValuesAdd(CEID1,'bob',Vbob)"));
	}
	
	@Test
	public void testMemberRepresentativCollector() throws IOException {
		if(logger == null){
			logger= LoggerFactory.getLogger(DispatcherTest.class);
		}
		
		// Get query.
		String queryString = BdplEleTest.getSparqlQuery("queries//bdpl-members-feature-given-event-id.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		
		EventMembersFromStream v = new EventMembersFromStream();
		Set<String> result = v.getMembersRepresentative(query);

		assertEquals(3, result.size());
		assertTrue(result.contains("Ve1"));
		assertTrue(result.contains("<http://events.event-processing.org/types/se>"));
		assertTrue(result.contains("<http://events.event-processing.org/types/st>"));

	}
	
	@Test
	public void testEventCounter() throws IOException {

		String queryString = getSparqlQuery("queries/bdpl-members-feature.eprq");
		Query query = null;

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}

		CountEventsVisitor v = new CountEventsVisitor();
		
		v.count(query.getEventQuery());
		
		assertEquals(2, (v.getNumberOfEvents()));
	}
	
	public static String getSparqlQuery(String queryFile) throws IOException {
		return IOUtils.toString(BdplEleTest.class.getClassLoader().getResourceAsStream(queryFile), StandardCharsets.UTF_8);
	}

}
