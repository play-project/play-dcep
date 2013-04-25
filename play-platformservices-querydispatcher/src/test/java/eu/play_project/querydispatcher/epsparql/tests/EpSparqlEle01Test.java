package eu.play_project.querydispatcher.epsparql.tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;

import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes;
import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes.AgregatedEventType;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.FilterExpressionCodeGenerator;


//import eu.play_project.querydispatcher.epsparql.tests.helpers.FilterExpressionCodeGenerator;

public class EpSparqlEle01Test {

	@Test
	public void manualParserUsage(){

		String queryString = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\nPREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\nPREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\nPREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX geo:     <http://www.w3.org/2003/01/geo/wgs84_pos#>\nPREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\nPREFIX :        <http://events.event-processing.org/types/>\n\nCONSTRUCT {\n\t:e rdf:type :SRBench-q2.\n\t:e :stream <http://streams.event-processing.org/ids/Srbench#stream>. \n}\nWHERE {\n\tWINDOW {\n\t\tEVENT ?id1 {\n\t\t\t?e1 :stream <http://streams.event-processing.org/ids/Srbench#stream>. \n\t\t\t?e1 om-owl:procedure ?sensor ;\n               om-owl:observedProperty weather:WindSpeed ;\n               om-owl:result [ om-owl:floatValue ?value ] .\n\t\t\t}\n\t} (\"PT60M\"^^xsd:duration, sliding)\n}HAVING (AVG(?value) >= \"74\"^^xsd:float )";
		Query query = null;
		
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch(Exception e){
			System.out.println("Exception was thrown: " + e);
			
		}
//		HavingVisitor v = new HavingVisitor();
//		
//		for (Expr el : query.getHavingExprs()) {
//			el.visit(v);
//		}

		// Use custom visitor
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		visitor1.setPatternId("'http://patternId.example.com/123456'");

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();
		

		System.out.println(etalisPattern);
	}
	
	
	
	@Test
	public void startParser() throws InterruptedException {
		String queryString = getQuery("EP-SPARQL-Query-HAVING.eprq")[0];
		//queryString = "PREFIX : <http://example.com> CONSTRUCT{:e :type :FacebookCepResult.} {EVENT ?id{?e1 :location [ :lat ?Latitude1; :long ?Longitude1 ]} GRAPH ?id{?s ?p ?o}}";
		System.out.println(queryString);
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);


		System.out.println("Querry \n" +query);

		// Use custom visitor
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		visitor1.setPatternId("'http://patternId.example.com/123456'");

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		System.out.println(etalisPattern);
		
	//	assertTrue(testEtalisPatter(etalisPattern));
	}


	
	@Test
	public void showQdResult(){

		String queryString;

		// Get query.
		queryString = getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);

		
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		System.out.println(etalisPattern);
	}
	
	@Test
	public void showEleResult() {

		String queryString;

		// Get query.
		queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
		System.out.println(queryString);
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		

		
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		System.out.println(etalisPattern);
		System.out.println(query);
	}

	@Test
	public void evaluateFilterExpression(){
		String queryString = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> CONSTRUCT{ ?x ?nice ?name. ?e rdf:type ?AlertEvent } WHERE {EVENT ?id{?e1 ?location \"abc\". ?e rdf:type ?AlertEvent} FILTER (abs(?Latitude1 - ?Latitude2) < 0.1 && abs(?Longitude1 - ?Longitude2) < 0.5)}";
		Query query = null;
		try{
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		}catch(Exception e){
			
		}
		FilterExpressionCodeGenerator visitor = new FilterExpressionCodeGenerator();
		
		//Get first EventGraph
		ElementEventGraph eventGraph = (ElementEventGraph)query.getEventQuery().get(0);
		
		Element filter = eventGraph.getFilterExp();
		
		visitor.startVisit(filter);
		
		System.out.println(visitor.getEle());
		
	}
//	@Test
//	public void dispatchQuery(){
//		String queryString = "CONSTRUCT{ ?x ?nice ?name } WHERE {EVENT ?id{?e1 ?location \"abc\"} FILTER (abs(?Latitude1 - ?Latitude2) < 0.1 && abs(?Longitude1 - ?Longitude2) < 0.5)}";
//		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
//		
//		VariableVisitor visitor = new VariableVisitor();
//	
//		Map<String, List<Variable>> variables =  visitor.getVariables(query, VariableTypes.historicType);
//		System.out.println(variables.values());
//	}
	
	@Test
	public void agregatedEventTypeTest(){
		AgregatedVariableTypes aTypes = new AgregatedVariableTypes();
		
		// Get query.
		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		
		// Get types.
		Map<String, AgregatedEventType> eventTypes = aTypes.detectType(query);
		
	
		//Test results.
		assertTrue(eventTypes.get("e3").equals(AgregatedEventType.H));
		assertTrue(eventTypes.get("e1").equals(AgregatedEventType.CR));
		assertTrue(eventTypes.get("bob").equals(AgregatedEventType.CR));
		assertTrue(eventTypes.get("e2").equals(AgregatedEventType.CR));
		assertTrue(eventTypes.get("direction").equals(AgregatedEventType.R));
//		assertTrue(eventTypes.get("firstEvent").equals(AgregatedEventType.R)); //FIXME Auch filter anschauen.
		assertTrue(eventTypes.get("alice").equals(AgregatedEventType.CR));
		assertTrue(eventTypes.get("tweetTime").equals(AgregatedEventType.H));
		assertTrue(eventTypes.get("tweetContent").equals(AgregatedEventType.CH));
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
			InputStream is = (InputStream) getClass().getClassLoader().getResource(queryFile).getContent();
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
	 * Returns the filenaes of the testfiles depending on the type of the
	 * testfile. The filename of a file with contains a broken query (a query
	 * which the parser do not acceapt) must start with "EP-SPARQL-BrokenQuery".
	 * The filename of a file with a regular query must start with
	 * "EP-SPARQL-Query".
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
	
	public static String getSparqlQuery(String queryFile) {
		try {
			InputStream is = EpSparqlEle01Test.class.getClassLoader().getResourceAsStream(queryFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;

			while (null != (line = br.readLine())) {
				sb.append(line);
				sb.append("\n");
			}
			// System.out.println(sb.toString());
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}