package eu.play_project.querydispatcher.bdpl.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
import eu.play_project.play_commons.constants.Namespace;
import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventIterator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventPatternOperatorCollector;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.FilterExpressionCodeGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.HavingVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.NotOperatorEleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.RdfQueryRepresentativeQueryVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;

public class BdplEleTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testManualParserUsage() throws IOException {

		String queryString = getSparqlQuery("queries/HistoricRealtimeQuery.eprq");
		Query query = null;
		System.out.println(queryString);

		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}

		EventIterator v = new EventIterator();
		query.getEventQuery().visit(v);
	}

	@Test
	public void testBasicEleGeneration() throws IOException {

		String queryString = getSparqlQuery("queries/HavingAvgExp2.eprq");
		Query query = null;

		System.out.println(queryString);

		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			query.setQueryId(patternId);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}

		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		// Add query details.
		QueryDetails details = new QueryDetails(patternId);
		// Set properties for windows in QueryDetails
		ElementWindowVisitor windowVisitor = new WindowVisitor(details);
		query.getWindow().accept(windowVisitor);
		details.setRdfDbQueries(visitor1.getRdfDbQueries());

		BdplQuery bdplQuery = BdplQuery.builder()
				.ele(etalisPattern)
				.details(details)
				.bdpl("")
				.constructTemplate(new QueryTemplateImpl())
				.historicalQueries(new LinkedList<HistoricalQuery>())
				.build();

		System.out.println(etalisPattern);
	}

	/**
	 * Check if code for all members will be generated.
	 */
	@Test
	public void testMembersFeature() throws IOException {

		String queryString = getSparqlQuery("queries/bdpl-members-feature.eprq");
		Query query = null;
		System.out.println(queryString);
		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			query.setQueryId(patternId);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}

		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		visitor1.generateQuery(query);
		String ele = visitor1.getEle();

		assertTrue(ele.contains("generateConstructResult('http://events.event-processing.org/types/e','http://events.event-processing.org/types/members',Ve1"));
		assertTrue(ele.contains("generateConstructResult('http://events.event-processing.org/types/e','http://events.event-processing.org/types/members',Ve2"));
	}

	@Test
	public void testVarEqualize() throws IOException {

		String queryString = getSparqlQuery("queries/play-bdpl-event-id-in-var.eprq");
		Query query = null;

		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			query.setQueryId(patternId);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}

		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();
		assertTrue(etalisPattern.contains(", Vid1=ViD"));
	}

	/**
	 * Check if wrong data type in filter will be detected.
	 */
	@Test
	public void filterTypeCheck() throws IOException {
		String queryString = getSparqlQuery("queries/DataTypeCheck.eprq");
		Query query = null;

		System.out.println(queryString);

		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";

		// Parse query
		query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		query.setQueryId(patternId);

		try {
			visitor1.generateQuery(query);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().toString()
					.contains("\"30\"^^xsd:stringis not a valid value in math expressions"));
		}
		String etalisPattern = visitor1.getEle();

		// Add query details.
		QueryDetails details = new QueryDetails(patternId);
		// Set properties for windows in QueryDetails
		ElementWindowVisitor windowVisitor = new WindowVisitor(details);
		query.getWindow().accept(windowVisitor);
		details.setRdfDbQueries(visitor1.getRdfDbQueries());

		System.out.println(etalisPattern);
	}

	@Test
	public void globalFilterVariables() throws IOException {

		String queryString = getSparqlQuery("play-bdpl-crisis-02b-windintensity.eprq");
		Query query = null;

		System.out.println(queryString);

		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			query.setQueryId(patternId);
		} catch (Exception e) {
			System.out.println("Exception while parsing the query: " + e);
		}

		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		visitor1.generateQuery(query);

		System.out.println(visitor1.getEle());
	}

	@Test
	public void complexFilterTest() throws IOException {

		String queryString = getSparqlQuery("play-bdpl-crisis-01b-radiationincrease.eprq");
		Query query = null;

		System.out.println(queryString);

		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			query.setQueryId(patternId);
		} catch (Exception e) {
			System.out.println("Exception while parsing the query: " + e);
		}

		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		visitor1.generateQuery(query);

		System.out.println(visitor1.getEle());
	}

	@Test
	public void orFilterTest() throws IOException {

		String queryString = getSparqlQuery("queries/play-bdpl-inria-green-services-01.eprq");
		Query query = null;

		System.out.println(queryString);

		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";
		

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			query.setQueryId(patternId);
		} catch (Exception e) {
			System.out.println("Exception while parsing the query: " + e);
		}

		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		visitor1.generateQuery(query);

		System.out.println(visitor1.getEle());
	}

	/**
	 * Generate code for (AVG(t) >= 30).
	 */
	@Test
	public void testHavingAvg() throws IOException {

		String queryString = getSparqlQuery("queries/HavingAvgExp2.eprq");
		Query query = null;

		System.out.println(queryString);
		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}
		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		// Generate code.
		HavingVisitor v = new HavingVisitor();

		for (Expr el : query.getHavingExprs()) {
			el.visit(v);
		}

		assertEquals("calcAverage(dbId0, 1800, Result10), greaterOrEqual(Result10,30.0)", v
				.getCode().toString());
	}

	@Test
	public void collectEventPatternsAndOperatorsTest1() throws IOException {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("queries/NestedEvent.eprq");

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		EventPatternOperatorCollector visitor1 = new EventPatternOperatorCollector();
		visitor1.collectValues(query.getEventQuery());
		
		Assert.assertEquals(3, visitor1.getEventPatterns().size());

		String[] expectedOperator = { "'SEQ'(", "'OR'", ")" };
		Assert.assertArrayEquals(expectedOperator, visitor1.getOperators().toArray());
	}

	@Test
	public void collectEventPatternsAndOperatorsTest2() throws IOException {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("queries/SimpleTree.eprq");

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		EventPatternOperatorCollector visitor1 = new EventPatternOperatorCollector();
		visitor1.collectValues(query.getEventQuery());
		
		Assert.assertEquals(3, visitor1.getEventPatterns().size());

		String[] expectedOperator = { "'SEQ'", "'OR'" };
		Assert.assertArrayEquals(expectedOperator, visitor1.getOperators().toArray());
	}
	
	@Test
	public void testNotOperatorCodeGeneration() throws IOException {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("queries/BDPL-Query-NotOperatorEvent.eprq");

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		// Get not pattern.
		EventPatternOperatorCollector visitor1 = new EventPatternOperatorCollector();
		visitor1.collectValues(query.getEventQuery());
		System.out.println(visitor1.getEventPatterns());

		Assert.assertEquals(1, visitor1.getEventPatterns().size());
		
		VariableTypeManager vtm = new VariableTypeManager(query);

		// Generate ELE.
		NotOperatorEleGenerator eleGenerator = new NotOperatorEleGenerator(vtm, "p1", "");
		
		visitor1.getEventPatterns().get(0).visit(eleGenerator);
		System.out.println(eleGenerator.getEle());
		
	}
	
	@Test
	public void testNotOperatorCodeGenerationWholePattern() throws IOException {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("queries/BDPL-Query-NotOperatorEvent.eprq");
		logger.debug("BDPL query: \n{}", queryString);
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		
		// Instantiate code generator
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		
		// Set id.
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";
				

		// Parse query
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			query.setQueryId(patternId);
		} catch (Exception e) {
			System.out.println("Exception while parsing the query: " + e);
		}

		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());

		visitor1.generateQuery(query);

		visitor1.getEle()
				.equals("\'http://events.event-processing.org/types/NoBiddersAlert\'(CEID1,example1) do (forall((\'dbQuery_example1_e1\'(ViD1, Ve2, Vid2)), (generateConstructResult(\'http://events.event-processing.org/types/e\',\'http://www.w3.org/1999/02/22-rdf-syntax-ns#type\',\'http://events.event-processing.org/types/NoBiddersAlert\',CEID1), generateConstructResult(\'http://events.event-processing.org/types/e\',\'http://events.event-processing.org/types/stream\',\'http://streams.event-processing.org/ids/fds#stream\',CEID1), generateConstructResult(\'http://events.event-processing.org/types/e\',\'http://events.event-processing.org/types/requestId\',VrequestId,CEID1), generateConstructResult(\'http://events.event-processing.org/types/e\',\'http://events.event-processing.org/types/members\',Ve3, CEID1), generateConstructResult(\'http://events.event-processing.org/types/e\',\'http://events.event-processing.org/types/members\',Ve2, CEID1), generateConstructResult(\'http://events.event-processing.org/types/e\',\'http://events.event-processing.org/types/members\',Ve1, CEID1))), decrementReferenceCounter(ViD1), decrementReferenceCounter(ViD2), decrementReferenceCounter(ViD3), constructResultIsNotEmpty(CEID1))<-(\'http://events.event-processing.org/types/DeliveryBid\'(ViD1) \'WHERE\' ((\\+forall(rdf(Ve3,\'http://www.w3.org/1999/02/22-rdf-syntax-ns#type\',\'http://events.event-processing.org/types/TimeOut\',ViD1), (\\+((\'dbQuery_example1_e1\'(ViD1, Ve2, Vid2)))))), Vid2=ViD1,  incrementReferenceCounter(ViD1)))\n");
	}

	@Test
	public void testShowQdResult() throws IOException {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		query.setQueryId(Namespace.PATTERN.getUri() + Math.random() * 1000000);
		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		System.out.println(etalisPattern);
	}

	@Test
	public void testShowEleResult() throws IOException {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("queries/HistoricRealtimeQuery.eprq");
		System.out.println(queryString);
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		query.setQueryId(Namespace.PATTERN.getUri() + Math.random() * 1000000);
		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		System.out.println(etalisPattern);
	}

	@Test
	public void testHistoricRealtimeSharedValues() throws IOException {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("queries/HistoricRealtimeQuery2.eprq");

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		query.setQueryId(Namespace.PATTERN.getUri() + 42);
		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();
		assertTrue(etalisPattern.contains("'screenName02',VscreenName02)))")
				|| etalisPattern.contains("'screenName02',VscreenName02)))"));
	}

	@Test
	public void testEvaluateFilterExpression() {
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> CONSTRUCT{ ?x ?nice ?name. ?e rdf:type ?AlertEvent } WHERE {EVENT ?id{?e1 ?location \"abc\". ?e rdf:type ?AlertEvent} FILTER (abs(?Latitude1 - ?Latitude2) < 0.1 && abs(?Longitude1 - ?Longitude2) < 0.5)}";
		Query query = null;
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {

		}
		FilterExpressionCodeGenerator visitor = new FilterExpressionCodeGenerator();

		// Get first EventGraph
		ElementEventGraph eventGraph = (ElementEventGraph) query.getEventQuery();

		Element filter = eventGraph.getFilterExp();

		visitor.startVisit(filter);

		System.out.println(visitor.getEle());

	}

	/**
	 * Return the query from given file. If given it returns the message of the
	 * expected exception.
	 * 
	 * @param queryFile
	 * @return query[0] the query text, query[1] if given in input file the
	 *         expected exception
	 */
	public String[] getQuery(String queryFile) {
		try {
			InputStream is = (InputStream) getClass().getClassLoader().getResource(queryFile)
					.getContent();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			StringBuffer sb = new StringBuffer();
			String line;
			String[] exeptionName = null;

			while ((line = br.readLine()) != null) {

				if (line.startsWith("#")) { // Line with comment
					sb.append(line);
					sb.append("\n"); // new Line after commentline
					// Extract exceptionname
					exeptionName = line.split(".*@expectedException<*| *\\\\>*");
				} else {
					sb.append(line);
				}
			}
			// System.out.println(sb.toString());
			br.close();
			isr.close();
			is.close();

			if (exeptionName != null && exeptionName.length > 1) {
				return new String[] { sb.toString(), exeptionName[1] };
			} else {
				return new String[] { sb.toString(), null };
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the filenaes of the test files depending on the type of the
	 * test file. The filename of a file with contains a broken query (a query
	 * which the parser do not accept) must start with "BDPL-BrokenQuery". The
	 * filename of a file with a regular query must start with "BDPL-Query".
	 * 
	 * @param dir
	 *            Directory of the files.
	 * @param type
	 *            Type of the fiel.
	 * @return All filenames with the specified type.
	 */
	public static List<String> getFilenames(File dir) {

		ArrayList<String> filenames = new ArrayList<String>();

		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().endsWith(".ele") && !files[i].isDirectory()) {
					filenames.add(files[i].getName());
				}
			}
		}
		return filenames;
	}

	public static String getSparqlQuery(String queryFile) throws IOException {
		return IOUtils.toString(BdplEleTest.class.getClassLoader().getResourceAsStream(queryFile),
				StandardCharsets.UTF_8);
	}

}
