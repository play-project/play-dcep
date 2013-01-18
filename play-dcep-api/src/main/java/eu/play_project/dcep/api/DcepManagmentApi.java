package eu.play_project.dcep.api;

import java.util.Map;

import eu.play_project.play_platformservices.api.EpSparqlQuery;


public interface DcepManagmentApi {

	public void registerEventPattern(EpSparqlQuery epSparqlQuery);
	public void unregisterEventPattern(String queryId);
	public EpSparqlQuery getRegisteredEventPattern(String queryId);
	public Map<String, EpSparqlQuery> getRegisteredEventPatterns();

}
