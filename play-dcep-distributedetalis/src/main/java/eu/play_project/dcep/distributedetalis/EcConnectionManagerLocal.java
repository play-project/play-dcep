package eu.play_project.dcep.distributedetalis;

import java.io.InputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
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
	public synchronized SelectResults getDataFromCloud(String query, String cloudId) throws EventCloudIdNotManaged, MalformedSparqlQueryException {
		 
		 
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
		
		ResultSetWrapper rw = result.getResult();
		return new ResultRegistry(rw);
	}

	public void setRdfModelinputFileName(String rdfModelinputFileName) {
		this.rdfModelinputFileName = rdfModelinputFileName;
	}

}
