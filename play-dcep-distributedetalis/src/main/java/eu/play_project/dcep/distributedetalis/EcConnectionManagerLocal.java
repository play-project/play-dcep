package eu.play_project.dcep.distributedetalis;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.VCARD;

import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;

public class EcConnectionManagerLocal extends EcConnectionManagerNet{
	
	String rdfModelinputFileName = "Example-historical-RDF-model.rdf";

	private static final long serialVersionUID = -9212054663979899431L;

	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {}
	
	@Override
	public void publish(CompoundEvent event) {}
	
	@Override
	public void unregisterEventPattern(EpSparqlQuery epSparqlQuery) {}
	
	@Override
	public synchronized SparqlSelectResponse getDataFromCloud(String query, String cloudId) throws EventCloudIdNotManaged, MalformedSparqlQueryException {
		 
		 
		 Model model = ModelFactory.createDefaultModel();
		 // use the FileManager to find the input file
		 InputStream in = FileManager.get().open(rdfModelinputFileName);
		if (in == null) {
		    throw new IllegalArgumentException("File: " + rdfModelinputFileName + " not found");
		}


		// Query data from model
		Query query1 = QueryFactory.create(query);
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
		
		return result;
	}

	public void setRdfModelinputFileName(String rdfModelinputFileName) {
		this.rdfModelinputFileName = rdfModelinputFileName;
	}

}
