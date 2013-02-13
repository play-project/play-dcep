package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

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
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_commons.eventtypes.EventHelpers;
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

}