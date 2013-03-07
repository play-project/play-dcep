package eu.play_project.querydispatcher.epsparql.tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;

import eu.play_project.play_platformservices.api.HistoricalQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.VariableQuadrupleVisitor;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.historic.QueryTemplateGenerator;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.StreamIdCollector;
import eu.play_project.play_platformservices_querydispatcher.types.C_Quadruple;
import eu.play_project.play_platformservices_querydispatcher.types.H_Quadruple;
import eu.play_project.play_platformservices_querydispatcher.types.R_Quadruple;
import fr.inria.eventcloud.api.Quadruple;
/**
 * 
 * @author sobermeier
 *
 */
public class DispatcherTests {
	private static Logger logger;

	
	@Test
	public void getIoStreamIds(){
		logger = LoggerFactory.getLogger(DispatcherTests.class);
		String queryString;
		Set<String> expectedInputStreams = new HashSet<String>(Arrays.asList(new String[] {"http://streams.event-processing.org/ids/TwitterFeed", "http://streams.event-processing.org/ids/TaxiUCGeoLocation", "http://streams.event-processing.org/ids/TaxiUCGeoLocation"}));
		
		
		// Get query.
		queryString = getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
		StreamIdCollector streamIdCollector = new StreamIdCollector();
	
		//
		QueryDetails qd = new QueryDetails();
		streamIdCollector.getStreamIds(query, qd);

		// Test output stream
		assertTrue(qd.getOutputStream().equals("http://streams.event-processing.org/ids/ContextualizedLatitudeFeed"));
		
		// Test input streams
		assertTrue(qd.getInputStreams().equals(expectedInputStreams));

	}
	
	@Test
	public void getVariablesAndTypes(){
		if(logger == null){
			logger= LoggerFactory.getLogger(DispatcherTests.class);
		}
		
		
		// Get query.
		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);

		
		VariableQuadrupleVisitor vqv = new VariableQuadrupleVisitor();
		
		Map<String, List<Quadruple>> variables = vqv.getVariables(query);
		
		//Print all variables and triples in which they occur.
		for(String key:variables.keySet()){
			logger.debug("Variable " + key + " occurs in: ");

			for (Quadruple quadruple : variables.get(key)) {
				//logger.debug(quadruple.toString());
				logger.debug("Type is: " +quadruple.getClass().getName());
				
				//Change Values
				if(quadruple.getClass().isInstance(H_Quadruple.class)){

				//Node node = quadruple.getObject(). ;
				//	node = Node.createURI("http");
				}
			}
			System.out.println();
		}
	}
	
	@Test
	public void showRealtimeHistoricVariables(){
		if(logger == null){
			logger= LoggerFactory.getLogger(DispatcherTests.class);
		}
		
		
		// Get query.
		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);

		
		VariableQuadrupleVisitor vqv = new VariableQuadrupleVisitor();
		
		Map<String, List<Quadruple>> variables = vqv.getVariables(query);
		
		System.out.println(vqv);
		
		//Result map.
		Map<String,Integer> variableAbsolutType = new HashMap<String, Integer>();
		
		//Print all variables and triples in which they occur.
		for(String key:variables.keySet()){
			logger.debug("Variable " + key + " occurs in: ");
			int type = 0;
			for (Quadruple quadruple : variables.get(key)) {
				logger.debug("Type is: " + quadruple.getClass().getName());

				if(quadruple instanceof C_Quadruple){
					type += 1;
				}
				if(quadruple instanceof R_Quadruple){
					type += 2;
				}
				if(quadruple instanceof H_Quadruple){
					type += 4;
				}
			}
			System.out.println(type);
			System.out.println();
		}
		
	}
	
	@Test
	public void dispatchQueryHistoricalMultipleClouds() {
		// Get query.
		String queryString = getSparqlQuery("EP-SPARQL-Query-Realtime-Historical-multiple-Clouds.eprq");

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);

		// Dispatch query
		List<HistoricalQuery> queries = PlaySerializer.serializeToMultipleSelectQueries(query);

		// Test results.
		String temperatureAstream = "PREFIX  : <http://events.event-processing.org/types/> \nPREFIX xsd : <http://events.event-processing.org/types/> \nPREFIX rdf : <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n\nSELECT DISTINCT  ?e2 ?temperature ?pub_date ?id2 ?e4 ?id4 \n WHERE { \nGRAPH ?id2\n  { ?e2 rdf:type :Temperature .\n    ?e2 :stream <http://streams.event-processing.org/ids/TemperatureA#stream> .\n    ?e2 :current ?temperature .\n    ?e2 :date ?pub_date\n  }\nGRAPH ?id4\n  { ?e4 rdf:type :Temperature .\n    ?e4 :stream <http://streams.event-processing.org/ids/TemperatureA#stream> .\n    ?e4 :current ?temperature\n }} ";
		String temperatureBstream = "PREFIX  : <http://events.event-processing.org/types/> \nPREFIX xsd : <http://events.event-processing.org/types/> \nPREFIX rdf : <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n\nSELECT DISTINCT  ?pub_date ?e3 ?id3 \n WHERE { \nGRAPH ?id3\n  { ?e3 rdf:type :Temperature .\n    ?e3 :stream <http://streams.event-processing.org/ids/TemperatureB#stream> .\n    ?e3 :date ?pub_date\n  }} \n";
		
		//Test if generated select query is OK.
		//assertTrue(queries.get(0).getQuery().equals(temperatureAstream));
		//assertTrue(queries.get(1).getQuery().equals(temperatureBstream));
		
		
		for (String varname : queries.get(0).getVariables()) {
			System.out.println(varname);
		}

	}
	
	
	
	@Test
	public void dispatchMissedCallsPlusTwitterQuery(){
		// Get query.
		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
		
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
		
		System.out.println(query);
		
		StreamIdCollector streamIdCollector = new StreamIdCollector ();
		
		QueryDetails qd = new QueryDetails();
		streamIdCollector.getStreamIds(query, qd);

		assertTrue(qd.getOutputStream().equals("http://streams.event-processing.org/ids/TaxiUCClic2Call"));
		assertTrue(qd.getInputStreams().contains("http://streams.event-processing.org/ids/TaxiUCCall"));
	}
	
	@Test
	public void testQueryTemplateGenerator(){
		 QueryTemplateGenerator ab  =  new  QueryTemplateGenerator();
		 
		// Get query.
		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
			
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
		
		QueryTemplate qt = ab.createQueryTemplate(query);
}
	
	private String getSparqlQuery(String queryFile) {
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
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
