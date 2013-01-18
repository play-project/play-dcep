package eu.play_project.play_platformservices.api;

import java.util.List;

import javax.jws.WebService;

import eu.play_project.play_platformservices.jaxb.Query;

/**
 * Accepts an EP-SPARQL query to decompose.
 * 
 * @author sobermeier
 * @author stuehmer
 */
@WebService
public interface QueryDispatchApi {
	/**
	 * To register a query with the PLAY platform.
	 * 
	 * @param queryId
	 *            An HTTP URI as an identifier for the query.
	 * @param query
	 *            EP-SPARQL query.
	 */
	public String registerQuery(String queryId, String epSparqlQuery);

	/**
	 * To unregister a query from the PLAY platform.
	 * 
	 * @param queryId
	 *            An HTTP URI as an identifier for the query.
	 */
	public void unregisterQuery(String queryId);

	/**
	 * To get details about a query. This operation does not register the query
	 * and can be used to do checks on the query before registering.
	 */
	public QueryDetails analyseQuery(String queryId, String query);

	/**
	 * Get a registered query identified by it's query ID which was used when
	 * registering it.
	 */
	public String getRegisteredQuery(String queryId);

	/**
	 * Get all currently registered queries.
	 */
	public List<Query> getRegisteredQueries();
}
