package eu.play_project.play_platformservices.api;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a SPARQL 1.1 query to get data from a triplestore.
 * It is often extracted from a BDPL query.
 * @author sobermeier
 *
 */
public class HistoricalQuery implements Serializable{
	
	private static final long serialVersionUID = 100L;
	private String cloudId;
	private String query;
	private boolean hasSharedVariablesWithRealtimePart = false;
	
	public HistoricalQuery() {
	}

	/**
	 * Get the id of the source of the data. The given query is dedicated to this cloudId.
	 * @return cloudId
	 */
	public String getCloudId() {
		return cloudId;
	}

	/**
	 * String representation of the cloud the query is dedicated to.
	 * @param cloudId Id of the cloud the query is dedicated to.
	 */
	public void setCloudId(String cloudId) {
		this.cloudId = cloudId;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean hasSharedVariablesWithRealtimePart() {
		return hasSharedVariablesWithRealtimePart;
	}

	public void hasSharedVariablesWithRealtimePart(boolean hasSharedVariablesWithRealtimePart) {
		this.hasSharedVariablesWithRealtimePart = hasSharedVariablesWithRealtimePart;
	}

}
