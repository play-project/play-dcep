package eu.play_project.dcep.distributedetalis;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.impl.jena.TypeConversion;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Syntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class EcConnectionManager4store extends EcConnectionManagerWsn {
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

	public EcConnectionManager4store(DistributedEtalis dEtalis) throws EcConnectionmanagerException {
		this(constants.getProperty("dcep.4store.rest"), dEtalis);
	}

	public EcConnectionManager4store(String fourStoreEndpoint, DistributedEtalis dEtalis)
			throws EcConnectionmanagerException {
		super(dEtalis);
		this.FOURSTORE_REST_URI = fourStoreEndpoint;

		init();
	}

	@Override
	public void init() throws EcConnectionmanagerException {
		super.init();

		fourStoreClient = ClientBuilder.newClient();
		
		dataEndpoint = fourStoreClient.target(FOURSTORE_REST_URI).path(DATA_PATH);
		updateEndpoint = fourStoreClient.target(FOURSTORE_REST_URI).path(UPDATE_PATH);
		sparqlEndpoint = fourStoreClient.target(FOURSTORE_REST_URI).path(SPARQL_PATH);
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

		logger.debug("Putting event in cloud '" + cloudId + "':\n" + query);

		Form form = new Form();
		form.param("update", query);

		Response response = updateEndpoint.request().post(
				Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

		if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
			logger.debug("Putting event in cloud '" + cloudId + "': successful: " + response);
		} else {
			logger.error("Putting event in cloud '" + cloudId + "': UNsuccessful: " + response);
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

		logger.debug("Putting event in cloud '" + cloudId + "':\n" + query);

		Form form = new Form();
		form.param("mime-type", Syntax.Turtle.getMimeType());
		form.param("graph", event.getGraph().toString());
		form.param("data", query);

		Response response = dataEndpoint.request().post(
				Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

		if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
			logger.debug("Putting event in cloud '" + cloudId + "': successful: " + response);
		} else {
			logger.error("Putting event in cloud '" + cloudId + "': UNsuccessful: " + response);
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

		logger.debug("Sending historical query to 4store: \n" + query);

		QueryResultTable sparqlResults = RDF2Go.getModelFactory().sparqlSelect(sparqlEndpoint.getUri().toString(), query);

		return ResultRegistry.makeResult(sparqlResults);
	}
}
