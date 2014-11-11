package eu.play_project.dcep.distributedetalis;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.dcep.node.api.EcConnectionManager;
import eu.play_project.dcep.node.api.EcConnectionmanagerException;
import eu.play_project.dcep.node.api.SelectResults;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.BdplQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

/**
 * An offline connection manager with local Jena e.g., to be used in Unit Tests.
 * 
 * @author Stefan Obermeier
 */
public class EcConnectionManagerLocalJena implements EcConnectionManager<CompoundEvent> {
	private static final long serialVersionUID = 100L;
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerLocal.class);
	private List<String> inputRdfModelFileName;
	private Map<String, ModelSet> tripleStores;
	
	// Local triplestores.
	public static final String COMPLEX_EVENT_TRIPLESTORE = "http://event-processing.org/local/complex#stream";
	public static final String HISTORIC_DATA_TRIPLESTORE = "http://event-processing.org/local/historic#stream";

	public EcConnectionManagerLocalJena(List<String> inputRdfModelFileName) {
		this.inputRdfModelFileName = inputRdfModelFileName;
		logger.info("Initialising {}.", this.getClass().getSimpleName());
		tripleStores = new HashMap<String, ModelSet>();
		
		tripleStores.put(EcConnectionManagerLocalJena.COMPLEX_EVENT_TRIPLESTORE, EventHelpers.createEmptyModelSet());
	}
	
	public EcConnectionManagerLocalJena(String inputRdfModelFileName) {
		this.inputRdfModelFileName = new LinkedList<String>();
		this.inputRdfModelFileName.add(inputRdfModelFileName);
		tripleStores.put(EcConnectionManagerLocalJena.COMPLEX_EVENT_TRIPLESTORE, EventHelpers.createEmptyModelSet());
		
		logger.info("Initialising {}.", this.getClass().getSimpleName());
	}

	public EcConnectionManagerLocalJena() {}

	@Override
	public void registerEventPattern(BdplQuery bdplQuery) {
	}

	@Override
	public void publish(CompoundEvent event) {
		logger.debug("Add event {} in local triplestore", event);
		tripleStores.get(EcConnectionManagerLocalJena.COMPLEX_EVENT_TRIPLESTORE).addModel(EventCloudHelpers.toRdf2go(event));
	}

	@Override
	public void unregisterEventPattern(BdplQuery bdplQuery) {
	}

	@Override
	public synchronized SelectResults getDataFromCloud(String query, String cloudId) throws EcConnectionmanagerException {
		ResultRegistry results = null;
		
		if (cloudId.equals(EcConnectionManagerLocalJena.HISTORIC_DATA_TRIPLESTORE)) {
			
			// Load data if needed.
			if (tripleStores.get(cloudId) == null) {
				ModelSet rdf = EventHelpers.createEmptyModelSet();
				
				tripleStores.put(cloudId, rdf);
				readDataFromFile(rdf);
				results =  queryDataFromModel(query, rdf);
			} else {
				results =  queryDataFromModel(query, tripleStores.get(EcConnectionManagerLocalJena.HISTORIC_DATA_TRIPLESTORE));
			}
		}
		else if (cloudId.equals(EcConnectionManagerLocalJena.COMPLEX_EVENT_TRIPLESTORE)) {
			results =  queryDataFromModel(query, tripleStores.get(cloudId));
		} else {
			throw new RuntimeException("Unknown cloud id: " + cloudId);
		}

		return (results);
	}

	@Override
	public void putDataInCloud(CompoundEvent event, String topic)
			throws EcConnectionmanagerException {
		
	}

	public void setInputRdfModelFileName(List<String> inputRdfModelFile) {
		this.inputRdfModelFileName = inputRdfModelFile;
	}

	@Override
	public void destroy() {
		logger.info("Terminating {}.", this.getClass().getSimpleName());
	}
	
	private ResultRegistry queryDataFromModel(String query, ModelSet rdf) {
		// Query data from model
		Query jenaQuery;
		Dataset jena;
		try {
			jenaQuery = QueryFactory.create(query);
			jena = (Dataset) rdf.getUnderlyingModelSetImplementation();
		} catch (QueryParseException e) {
			logger.error("Query with pars error: {}", query);
			throw e;
		}
			
		logger.debug("Execute historical query: {}", query);
		QueryExecution qexec = QueryExecutionFactory.create(jenaQuery, jena);
		
		ResultRegistry results = null;
		try {
			results = ResultRegistry.makeResult(new ResultSetWrapper(qexec.execSelect()));
		} finally {
			qexec.close();
		}
		
		return results;
	}
	
	private void readDataFromFile(ModelSet rdf) {
		if (inputRdfModelFileName == null) {
			throw new RuntimeException("No data in jena model.");
		}

		for (String historicDataFileName: inputRdfModelFileName) {
			InputStream in = this.getClass().getClassLoader()
					.getResourceAsStream(historicDataFileName);
			if (in == null) {
				throw new IllegalArgumentException("File: " + historicDataFileName + " not found");
			}

			// Read data from file.
			try {
				logger.debug("Read historical data from file: {}", inputRdfModelFileName);
				rdf.readFrom(in, Syntax.forFileName(historicDataFileName));
			} catch (SyntaxNotSupportedException e) {
				logger.error("Syntax {} is not supported.", Syntax.Turtle);
				e.printStackTrace();
			} catch (ModelRuntimeException e) {
				logger.error("ModelRuntimeException: {}", e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("IO-Exception: {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
