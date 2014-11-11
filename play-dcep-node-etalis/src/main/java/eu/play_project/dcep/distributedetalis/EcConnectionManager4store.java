package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_EXIT;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_EXIT;

import java.io.ByteArrayOutputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.impl.jena.TypeConversion;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Syntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;

import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerRest;
import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerWsn;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.dcep.node.api.EcConnectionmanagerException;
import eu.play_project.dcep.node.api.SelectResults;
import eu.play_project.dcep.node.connections.AbstractConnectionManagerWsn;
import eu.play_project.play_commons.constants.Event;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;

/**
 * The connection manager to get real-time events from the PLAY Platform and get
 * historical data from the RDF store <a href="http://4store.org/">4store</a>.
 * 
 * @author Roland Stühmer
 */
public class EcConnectionManager4store extends AbstractConnectionManagerWsn<CompoundEvent> {
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManager4store.class);
	private Client fourStoreClient;
	private final String FOURSTORE_REST_URI;
	private WebTarget dataEndpoint;
	private WebTarget updateEndpoint;
	private WebTarget sparqlEndpoint;
	/*
	 * TODO stuehmer: to enable federated queries with arbitrary 4store nodes,
	 * we can use the cloudId in the future instead of this constant
	 * sparqlEndpoint
	 */
	public static final String SPARQL_PATH = "sparql/";
	public static final String DATA_PATH = "data/";
	public static final String UPDATE_PATH = "update/";
	public static final String STATUS_PATH = "status/";

	public EcConnectionManager4store(DistributedEtalis dEtalis) throws EcConnectionmanagerException {
		this(constants.getProperty("dcep.4store.rest"), dEtalis);
	}

	public EcConnectionManager4store(String fourStoreEndpoint, DistributedEtalis dEtalis)
			throws EcConnectionmanagerException {
		super(dEtalis);
		this.FOURSTORE_REST_URI = fourStoreEndpoint;

		init();
	}

	public void init() throws EcConnectionmanagerException {
		AbstractReceiverRest receiver = new AbstractReceiverRest() {};
		super.init(new EcConnectionListenerWsn(receiver), new EcConnectionListenerRest(receiver));

		fourStoreClient = ClientBuilder.newClient();
		
		dataEndpoint = fourStoreClient.target(FOURSTORE_REST_URI).path(DATA_PATH);
		updateEndpoint = fourStoreClient.target(FOURSTORE_REST_URI).path(UPDATE_PATH);
		sparqlEndpoint = fourStoreClient.target(FOURSTORE_REST_URI).path(SPARQL_PATH);
		
		// Run a quick sanity check on 4store:
		try {
			Response fourstoreCheck = fourStoreClient.target(FOURSTORE_REST_URI).path(STATUS_PATH).request().head();
			if (fourstoreCheck.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
				throw new EcConnectionmanagerException(String.format("4store returned response '%s', possible misconfiguration.", fourstoreCheck.getStatusInfo().toString()));
			}
			fourstoreCheck.close();
		} catch (Exception e) {
			throw new EcConnectionmanagerException(String.format("4store returned exception '%s', possible misconfiguration: %s", e.getClass().getSimpleName(), e.getMessage()));
		}
	}

	@Override
	public void destroy() {
		if (fourStoreClient != null) {
			fourStoreClient.close();
		}

		super.destroy();
	}

	/**
	 * Persist data in historic storage.
	 * 
	 * @param event
	 *            event containing quadruples
	 * @param cloudId
	 *            the cloud ID to allow partitioning of storage
	 */
	@Override
	public void putDataInCloud(CompoundEvent event, String cloudId) {
		// Chose one implementation (SPARQL Update or SPARQL Graph Store
		// Protocol):
		// SPARQL Update is currently faster:
		putDataInCloudUsingSparqlUpdate(event, cloudId);
	}

	/**
	 * Persist data in historic storage.
	 * 
	 * @param event
	 *            event containing quadruples
	 * @param cloudId
	 *            the cloud ID to allow partitioning of storage
	 */
	public void putDataInCloudUsingSparqlUpdate(CompoundEvent event, String cloudId) {

		StringBuilder s = new StringBuilder();
		s.append("INSERT DATA { GRAPH <").append(event.getGraph().toString()).append("> {\n");
		for (Quadruple quadruple : event) {
			s.append(TypeConversion.toRDF2Go(quadruple.getSubject()).toSPARQL()).append(" ");
			s.append(TypeConversion.toRDF2Go(quadruple.getPredicate()).toSPARQL()).append(" ");
			s.append(TypeConversion.toRDF2Go(quadruple.getObject()).toSPARQL()).append(" . \n");
		}
		s.append("}}\n");
		String query = s.toString();

		logger.debug("Putting event in cloud '{}':\n{}", cloudId, query);

		Form form = new Form();
		form.param("update", query);

		Response response = updateEndpoint.request().post(
				Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

		if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
			logger.debug("Putting event in cloud '{}': successful: {}", cloudId, response);
		} else {
			logger.error("Putting event in cloud '{}': UNsuccessful: {}", cloudId, response);
		}
		response.close();
	}

	/**
	 * Persist data in historic storage.
	 * 
	 * @param event
	 *            event containing quadruples
	 * @param cloudId
	 *            the cloud ID to allow partitioning of storage
	 */
	public void putDataInCloudUsingGraphStoreProtocol(CompoundEvent event, String cloudId) {

		String query = EventCloudHelpers.toRdf2go(event).serialize(Syntax.Turtle);

		logger.debug("Putting event in cloud '{}':\n{}", cloudId, query);

		Form form = new Form();
		form.param("mime-type", Syntax.Turtle.getMimeType());
		form.param("graph", event.getGraph().toString());
		form.param("data", query);

		Response response = dataEndpoint.request().post(
				Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

		if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
			logger.debug("Putting event in cloud '{}': successful: {}", cloudId, response);
		} else {
			logger.error("Putting event in cloud '{}': UNsuccessful: {}", cloudId, response);
		}
		response.close();
	}

	/**
	 * Retreive data from historic storage using a SPARQL SELECT query. SPARQL
	 * 1.1 enhancements like the VALUES clause are allowed.
	 */
	@Override
	public SelectResults getDataFromCloud(String query, String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		logger.debug("Sending historical query to 4store: \n{}", query);

		QueryResultTable sparqlResults = RDF2Go.getModelFactory().sparqlSelect(sparqlEndpoint.getUri().toString(), query);

		return ResultRegistry.makeResult(sparqlResults);
	}

	@Override
	public void publish(CompoundEvent event) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		String cloudId = EventCloudHelpers.getCloudId(event);
	    
		if (!cloudId.isEmpty()) {
			// Send event to DSB:
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RDFDataMgr.write(out, quadruplesToDatasetGraph(event), RDFFormat.TRIG_BLOCKS);
	
			// Do not remove this line, needed for logs. :stuehmer
			logger.info(LOG_DCEP_EXIT + event.getGraph() + " " + EventCloudHelpers.getMembers(event));
			if (logger.isDebugEnabled()) {
				logger.debug(LOG_DCEP + "Complex Event:\n{}", event.toString());
			}
			
			this.getRdfSender().notify(new String(out.toByteArray()), cloudId);
			
			// Store event in Triple Store:
			this.putDataInCloud(event, cloudId);
		}
		else {
			logger.warn(LOG_DCEP_FAILED_EXIT + "Got empty cloud ID from event '{}', don't know which cloud to publish to. Discarding complex event.", event.getGraph() + Event.EVENT_ID_SUFFIX);
		}
	}

	/**
	 * A private method to convert a collection of quadruples into the
	 * corresponding data set graph to be used in the event format writers
	 * 
	 * @author ialshaba
	 * 
	 * @param quads
	 *            the collection of the quadruples
	 * @return the corresponding data set graph
	 */
	private static DatasetGraph quadruplesToDatasetGraph(CompoundEvent quads) {
	    DatasetGraph dsg = DatasetGraphFactory.createMem();
	    for (Quadruple q : quads) {
	        if (q.getPredicate() != PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE) {
	            dsg.add(
	                    q.getGraph(), q.getSubject(), q.getPredicate(),
	                    q.getObject());
	        }
	    }
	
	    return dsg;
	}
}
