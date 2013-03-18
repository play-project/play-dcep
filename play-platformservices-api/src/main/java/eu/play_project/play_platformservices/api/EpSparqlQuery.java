package eu.play_project.play_platformservices.api;

import java.io.Serializable;
import java.util.List;

public class EpSparqlQuery implements Serializable {

	private static final long serialVersionUID = -3987453668321624829L;

	private QueryDetails queryDetails;
	private String epSparqlQuery;
	private String eleQuery;
	private QueryTemplate constructTemplate;
	private List<HistoricalQuery> historicalQueries;
	
	
	public EpSparqlQuery(){}
	public EpSparqlQuery(QueryDetails queryDetails, String eleQuery){
		this.queryDetails = queryDetails;
		this.eleQuery = eleQuery;
	}
	
	public QueryDetails getQueryDetails() {
		return queryDetails;
	}
	
	public void setQueryDetails(QueryDetails queryDetails) {
		this.queryDetails = queryDetails;
	}
	
	public String getEpSparqlQuery() {
		return epSparqlQuery;
	}
	
	public void setEpSparqlQuery(String epSparqlQuery) {
		this.epSparqlQuery = epSparqlQuery;
	}
	
	public String getEleQuery() {
		return eleQuery;
	}

	public QueryTemplate getConstructTemplate() {
		if(constructTemplate == null){
			throw new RuntimeException("No construct template found.");
		}else{
			return constructTemplate;
		}
	}
	
	/**
	 * Set template for historical queries with placeholder for shared variables.
	 * @param constructTemplate
	 */
	public void setConstructTemplate(QueryTemplate constructTemplate) {
		this.constructTemplate = constructTemplate;
	}
	
	public List<HistoricalQuery> getHistoricalQueries() {
		return historicalQueries;
	}
	
	public void setHistoricalQueries(List<HistoricalQuery> historicalQueries) {
		this.historicalQueries = historicalQueries;
	}
	public void setEleQuery(String eleQuery) {
		this.eleQuery = eleQuery;
	}
	
}
