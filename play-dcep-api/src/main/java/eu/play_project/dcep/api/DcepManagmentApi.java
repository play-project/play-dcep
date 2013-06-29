package eu.play_project.dcep.api;

import java.util.Map;

import eu.play_project.play_platformservices.api.CepQuery;


public interface DcepManagmentApi {

	/**
	 * Register a new event pattern.
	 * 
	 * @throws DcepManagementException
	 *             if the pattern has errors
	 */
	public void registerEventPattern(CepQuery cepQuery);

	/**
	 * Unregister an existing event pattern. This method silently does nothing
	 * if there is no pattern with the given ID.
	 * 
	 * @param queryId
	 */
	public void unregisterEventPattern(String queryId);

	/**
	 * Get a previously registered event pattern.
	 * 
	 * @throws DcepManagementException
	 *             if there is no pattern with the given ID
	 */
	public CepQuery getRegisteredEventPattern(String queryId) throws DcepManagementException;

	/**
	 * Get a collection of all currently registered event patterns.
	 */
	public Map<String, CepQuery> getRegisteredEventPatterns();

}
