package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.VCARD;

import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

/**
 * Tests of the local version of EcConnectionManager.
 * @author Stefan Obermeier
 *
 */
public class EcConnectinManagerLocalTest {

	/**
	 * Create  Jena model and query some data.
	 */
	@Test
	public void generateQueryResult() {
		
		Model model = ModelFactory.createDefaultModel();

		Resource modelTests1 = model
				.createResource("http://play-project.eu/modelTest");
		modelTests1.addProperty(VCARD.FN, "1234");

		Resource modelTests2 = model
				.createResource("http://play-project.eu/modelTest");
		modelTests2.addProperty(VCARD.FN, "4321");

		// Query data from model
		Query query = QueryFactory.create("SELECT ?O WHERE {?S ?P ?O}");
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		SparqlSelectResponse result;
		try {
			ResultSet results = qexec.execSelect();

			// Put result in PLAY result wrapper.
			ResultSetWrapper dataIn = new ResultSetWrapper(results);
			result = new SparqlSelectResponse(1, 1, 1, 1, dataIn);
		} finally {
			qexec.close();
		}

		assertTrue(result.getResult().next().get("O").toString().equals("4321"));
		assertTrue(result.getResult().next().get("O").toString().equals("1234"));
		
//		//Print results. (Copy form join-engine)
//		while (result.getResult().hasNext()) {
//			QuerySolution data = result.getResult().next();
//			Iterator<String> iter1 = data.varNames();
//
//			// Print variable name and value.
//			while (iter1.hasNext()) {
//				String next = iter1.next();
//				System.out.println(next + "\t" + data.get(next));
//			}
//		}

	}
	
	@Test
	public void readModelFromFile(){
		// create an empty model
		 Model model = ModelFactory.createDefaultModel();
		 
		 String inputFileName = "Example-historical-RDF-model.rdf";
		 // use the FileManager to find the input file
		 InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
		    throw new IllegalArgumentException("File: " + inputFileName + " not found");
		}

		model.read(in, null);

		// Query data from model
		Query query = QueryFactory.create("SELECT ?O WHERE {?S ?P ?O}");
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		SparqlSelectResponse result;
		try {
			ResultSet results = qexec.execSelect();

			// Put result in PLAY result wrapper.
			ResultSetWrapper dataIn = new ResultSetWrapper(results);
			result = new SparqlSelectResponse(1, 1, 1, 1, dataIn);
		} finally {
			qexec.close();
		}

		assertTrue(result.getResult().next().get("O").toString().equals("http://demo.play-project.eu/"));
		assertTrue(result.getResult().next().get("O").toString().equals("VesselInformation"));
		assertTrue(result.getResult().next().get("O").toString().equals("Clic2Call"));
	}

}
