package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_EXIT;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

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
 * An offline connection manager e.g., to be used in Unit Tests.
 * 
 * @author Stefan Obermeier
 */
public class EcConnectionManagerLocal implements Serializable, EcConnectionManager<CompoundEvent> {

	private static final long serialVersionUID = 100L;
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerLocal.class);
	private List<String> inputRdfModelFileName;

	public EcConnectionManagerLocal(List<String> inputRdfModelFileName) {
		this.inputRdfModelFileName = inputRdfModelFileName;
		logger.info("Initialising {}.", this.getClass().getSimpleName());
	}
	
	public EcConnectionManagerLocal(String inputRdfModelFileName) {
		this.inputRdfModelFileName = new LinkedList<String>();
		this.inputRdfModelFileName.add(inputRdfModelFileName);
		
		logger.info("Initialising {}.", this.getClass().getSimpleName());
	}

	public EcConnectionManagerLocal() {
	}

	@Override
	public void registerEventPattern(BdplQuery bdplQuery) {
	}

	@Override
	public void publish(CompoundEvent event) {
		logger.info(LOG_DCEP_EXIT + event.getGraph() + " " + EventCloudHelpers.getMembers(event));
	}

	@Override
	public void unregisterEventPattern(BdplQuery bdplQuery) {
	}

	@Override
	public synchronized SelectResults getDataFromCloud(String query,
			String cloudId) throws EcConnectionmanagerException {
		// Create an empty model.
		ModelSet rdf = EventHelpers.createEmptyModelSet();

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

		return (results);
	}

	@Override
	public void putDataInCloud(CompoundEvent event, String topic)
			throws EcConnectionmanagerException {
		throw new UnsupportedOperationException("not implemented");
	}

	public void setInputRdfModelFileName(List<String> inputRdfModelFile) {
		this.inputRdfModelFileName = inputRdfModelFile;
	}

	@Override
	public void destroy() {
		logger.info("Terminating {}.", this.getClass().getSimpleName());
	}
}
