//package eu.play_project.dcep.distribution.eventcloud.remotetests;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.Syntax;
//
//import eu.play_project.dcep.distributedetalis.EcConnectionManager;
//import eu.play_project.play_commons.constants.Constants;
//import eu.play_project.play_commons.constants.Stream;
//import eu.play_project.play_platformservices.api.CepQuery;
//import eu.play_project.play_platformservices.api.QueryDetails;
//import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
//import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.EleGeneratorForConstructQuery;
//import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
//
//public class HistoricalQuery {
//
//
//	public static void main(String[] args) throws IOException {
//		Logger logger = LoggerFactory.getLogger(HistoricalQuery.class);
//		
//		//Get URI for EventCloud registry.
//		//final String eventCloudRegistry = Constants.getProperties().getProperty("eventcloud.unstable.registry");
//		final String eventCloudRegistry = Constants.getProperties().getProperty("eventcloud.registry");
//		
//		//Start connection manager.
//		EcConnectionManager ecConnection = new EcConnectionManager(eventCloudRegistry);
//
//		// Example shared variable values.
//		Map<String, List<String>> variableBindings = new HashMap<String, List<String>>();
//	
//		//Values for variable A
//		List<String> valesA =  new  LinkedList<String>();
//		valesA.add("30");
//		valesA.add("25");
//		valesA.add("23");
//		
//		variableBindings.put("temperature", valesA);
//		
//		// Query
//		//Example EP-SPARQL-Query-Realtime-Historical-shared-Variables.eprq
//		String queryString = "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX xsd:       <http://events.event-processing.org/types/>\nPREFIX :       <http://events.event-processing.org/types/>\n CONSTRUCT{\n    :e rdf:type :Temperature .\n    :e :stream <http://streams.event-processing.org/ids/Temperatures#stream>.\n    :e :current    ?temperature.\n    :e :date ?pub_date \n }\n WHERE{\n \tEVENT ?id1 {\n   \t\t?e1 rdf:type :Temperature .\n   \t\t?e1 :stream <http://streams.event-processing.org/ids/Temperature#stream> .\n   \t\t?e1 :current ?temperature .\n \t}\n \tGRAPH ?id {\n   \t\t?e1 rdf:type :Temperature .\n   \t\t?e1 :current ?temperature .\n   \t\t?e2 :date ?pub_date \n  \t}\n }";
//		//CepQuery query = generateQuery(args[0]);
//		CepQuery query = generateQuery(queryString) ; 
//		String historicalQuery = query.getHistoricalQuery();
//		
//		//TODO Add values.
//		SPARQLParser sp = new SPARQLParser(variableBindings);
//		historicalQuery = sp.addVALUESBlocks(historicalQuery);
//		
//		
//		// Test if historical query is a valid SPARQL query.
//		System.out.println("Historical query" + historicalQuery);
//		Query q = QueryFactory.create(query.getHistoricalQuery(), Syntax.syntaxSPARQL_11);
//
//		
//		
//		// Request historical data.
//		SparqlSelectResponse result = ecConnection.getDataFromCloud(historicalQuery, Stream.TwitterFeed.getTopicUri());
//		
//		//Print historical data.
//		while (result.getResult().hasNext()) {
//			com.hp.hpl.jena.query.QuerySolution data = result.getResult().next();
//			Iterator<String> iter = data.varNames();
//			
//			// Print variablename and value.
//			while(iter.hasNext()){
//				String next = iter.next();
//				System.out.println(next + "\t" + data.get(next));
//			}
//		}
//
//	}
//	
//
//	public static CepQuery generateQuery(String queryString){
//		com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString, Syntax.syntaxEPSPARQL_20);
//
//		EleGenerator eleGenerator = new EleGeneratorForConstructQuery();
//		eleGenerator.setPatternId("'id_1234'");
//		eleGenerator.generateEle(query);
//		
//		CepQuery epQuery = new CepQuery(new QueryDetails("'id_1234'"), eleGenerator.getEle());
//		epQuery.setHistoricalQuery(query.toString());
//		
//		return epQuery;
//
//	}
//	
//	private String getSparqlQuerys(String queryFile) {
//		try {
//			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));
//			StringBuffer sb = new StringBuffer();
//			String line;
//
//			while (null != (line = br.readLine())) {
//				sb.append(line);
//				sb.append("\n");
//			}
//			// System.out.println(sb.toString());
//			br.close();
//			is.close();
//
//			return sb.toString();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//
//	}
//}
