package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.exception.SyntaxNotSupportedException;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.Syntax;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerLocal;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.api.VariableBindings;
import eu.play_project.dcep.distributedetalis.join.Engine;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

/**
 * Tests of the local version of EcConnectionManager.
 * 
 * @author Stefan Obermeier
 * 
 */
public class EcConnectionManagerLocalTest {

	/**
	 * Read Model from file and test basic graph query.
	 */
	@Test
	public void readModelFromFile() throws SyntaxNotSupportedException, ModelRuntimeException, IOException {
		// Create an empty model.
		ModelSet rdf = EventHelpers.createEmptyModelSet();

		String inputFileName = "Example-historical-RDF-model.trig";
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(inputFileName);
		if (in == null) {
			throw new IllegalArgumentException("File: " + inputFileName+ " not found");
		}

		// Read data from file.
		rdf.readFrom(in, Syntax.Trig);

		// Query data from model
		Query query = QueryFactory.create("SELECT ?O WHERE { GRAPH ?id {?S <http://events.event-processing.org/types/screenName> \"roland.stuehmer\"." +
																		"?S <http://events.event-processing.org/types/twitterName> ?O}}");
		Dataset jena = (Dataset) rdf.getUnderlyingModelSetImplementation();
		
		QueryExecution qexec = QueryExecutionFactory.create(query, jena);

		SparqlSelectResponse result;
		try {
			ResultSet results = qexec.execSelect();

			// Put result in PLAY result wrapper.
			ResultSetWrapper dataIn = new ResultSetWrapper(results);
			result = new SparqlSelectResponse(1, 1, 1, 1, dataIn);
		} finally {
			qexec.close();
		}

		assertTrue(result.getResult().next().get("O").toString()
				.equals("Roland St\u00FChmer"));
	}
	
	@Test
	public void queryEcConnectionManagerLocal() throws EcConnectionmanagerException, MalformedSparqlQueryException{
		EcConnectionManagerLocal ecm =  new EcConnectionManagerLocal("Example-historical-RDF-model.trig");
		
		String query = "SELECT ?O WHERE { GRAPH ?id {?S <http://events.event-processing.org/types/screenName> \"roland.stuehmer\"." +
				"?S <http://events.event-processing.org/types/twitterName> ?O}}";

		SelectResults sr = ecm.getDataFromCloud(query, "local");
		
		assertEquals(sr.getResult().get(0).get(0).toString(), "Roland Stühmer");
	}
	
	@Test
	public void executeExamplePlayBdplClic2callPlusTweetHistoricalQuery() throws EcConnectionmanagerException, MalformedSparqlQueryException{
		String query = "PREFIX sioc: <http://rdfs.org/sioc/ns#> \nPREFIX : <http://events.event-processing.org/types/> \nPREFIX uctelco: <http://events.event-processing.org/uc/telco/> \nPREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n\nSELECT DISTINCT  ?e3 ?tweetTime ?firstEvent ?tweetContent ?id3 ?firstEvent \n WHERE { \nGRAPH ?id3\n  { ?e3 rdf:type :TwitterEvent .\n    ?e3 :stream <http://streams.event-processing.org/ids/TwitterFeed#stream> .\n    ?e3 :endTime ?tweetTime .\n    ?e3 :test ?firstEvent .\n    ?e3 sioc:content ?tweetContent\n\t FILTER ( ?tweetTime > ?firstEvent )\n    }} \n VALUES (?firstEvent) {\n(\"2013-08-24T12:42:01.011Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) }\n ";
		
		EcConnectionManagerLocal ecm =  new EcConnectionManagerLocal("play-epsparql-clic2call-historical-data.trig");
		
		SelectResults sr = ecm.getDataFromCloud(query, "local");
		System.out.println(sr.getResult().get(0));
		assertTrue(sr.getResult().get(0).get(0).equals("http://events.event-processing.org/ids/e5917518587088559184#event"));
		assertTrue(sr.getResult().get(0).get(1).equals("2999-08-24T12:42:01.011Z^^http://www.w3.org/2001/XMLSchema#dateTime"));
		assertTrue(sr.getResult().get(0).get(2).equals("2013-08-24T12:42:01.011Z^^http://www.w3.org/2001/XMLSchema#dateTime"));
		assertTrue(sr.getResult().get(0).get(3).equals("Tweettext 1"));
		assertTrue(sr.getResult().get(0).get(4).equals("http://events.event-processing.org/ids/e5917518587088559184"));
	}
	
	@Test
	public void testHistoricalDataAndJoin() throws EcConnectionmanagerException, MalformedSparqlQueryException{
		// Prepare some input objects
		List<HistoricalQuery> list = new ArrayList<HistoricalQuery>();
		HistoricalQuery hq = new HistoricalQuery();
		hq.setCloudId("local");
		hq.setQuery("PREFIX sioc: <http://rdfs.org/sioc/ns#> \nPREFIX : <http://events.event-processing.org/types/> \nPREFIX uctelco: <http://events.event-processing.org/uc/telco/> \nPREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n\nSELECT DISTINCT  ?e3 ?tweetTime ?firstEvent ?tweetContent ?id3 ?firstEvent \n WHERE { \nGRAPH ?id3\n  { ?e3 rdf:type :TwitterEvent .\n    ?e3 :stream <http://streams.event-processing.org/ids/TwitterFeed#stream> .\n    ?e3 :endTime ?tweetTime .\n    ?e3 :test ?firstEvent .\n    ?e3 sioc:content ?tweetContent\n\t FILTER ( ?tweetTime > ?firstEvent )\n    }} \n ");
		list.add(hq);
		VariableBindings variableBindings = new VariableBindings();
		variableBindings.put("?e3", new ArrayList<Object>());
		variableBindings.put("?tweetTime", new ArrayList<Object>());
		variableBindings.put("?firstEvent", new ArrayList<Object>());
		variableBindings.put("?tweetContent", new ArrayList<Object>());
		variableBindings.put("?id3", new ArrayList<Object>());
		variableBindings.put("?firstEvent", new ArrayList<Object>());
			
		EcConnectionManagerLocal ecm =  new EcConnectionManagerLocal("play-epsparql-clic2call-historical-data.trig");
		
		Engine historicData = new Engine(ecm);
		HistoricalData values = historicData.get(list, variableBindings);

		System.out.println(values);
	}

}