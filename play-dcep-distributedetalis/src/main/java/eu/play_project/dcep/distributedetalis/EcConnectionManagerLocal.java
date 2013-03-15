package eu.play_project.dcep.distributedetalis;

import java.io.IOException;
import java.io.InputStream;

import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.exception.SyntaxNotSupportedException;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.Syntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;

import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

public class EcConnectionManagerLocal extends EcConnectionManagerNet{

	private static final long serialVersionUID = -9212054663979899431L;
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerLocal.class);
	private String inputRdfModelFileName;

	public EcConnectionManagerLocal(String inputRdfModelFileName){
		this.inputRdfModelFileName = inputRdfModelFileName;
	}
	
	public EcConnectionManagerLocal(){}
	
	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {}
	
	@Override
	public void publish(CompoundEvent event) {}
	
	@Override
	public void unregisterEventPattern(EpSparqlQuery epSparqlQuery) {}
	
	@Override
	public synchronized SelectResults getDataFromCloud(String query,
			String cloudId) throws EcConnectionmanagerException {
		// Create an empty model.
		ModelSet rdf = EventHelpers.createEmptyModelSet();

		if(inputRdfModelFileName==null){
			throw new RuntimeException("No data in jena model.");
		}
		
	
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(inputRdfModelFileName);
		if (in == null) {
			throw new IllegalArgumentException("File: " + inputRdfModelFileName + " not found");
		}

		// Read data from file.
		try {
			logger.debug("Read historical data from file: " + inputRdfModelFileName + ".");
			rdf.readFrom(in, Syntax.Trig);
		} catch (SyntaxNotSupportedException e) {
			logger.error("Syntax " + Syntax.Trig + " is not supported." );
			e.printStackTrace();
		} catch (ModelRuntimeException e) {
			logger.error("ModelRuntimeException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IO-Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Query data from model
		Query jenaQuery;
		Dataset jena;
		try{
			jenaQuery = QueryFactory.create(query);
			jena = (Dataset) rdf.getUnderlyingModelSetImplementation();
		}catch(QueryParseException e){
			logger.error("Query with pars error: " + query);
			throw e;
		}
		
		logger.debug("Execute historical query: " + query);
		QueryExecution qexec = QueryExecutionFactory.create(jenaQuery, jena);

		ResultRegistry results = null;
		try {
			results = new ResultRegistry(new ResultSetWrapper(qexec.execSelect()));
		} finally {
			qexec.close();
		}

		return (results);
	}

	public void setInputRdfModelFileName(String inputRdfModelFile) {
		this.inputRdfModelFileName = inputRdfModelFile;
	}
}
