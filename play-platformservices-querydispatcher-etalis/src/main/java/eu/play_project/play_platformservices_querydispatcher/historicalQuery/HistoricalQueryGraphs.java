package eu.play_project.play_platformservices_querydispatcher.historicalQuery;

/**
 * To store historical queries and destination cloud.
 * @author sobermeier
 *
 */
public class HistoricalQueryGraphs {
	
	private String cloudId;
	private String graphs;
	
	public HistoricalQueryGraphs(String graphs, String cloudId) {
		this.graphs = graphs;
		this.cloudId = cloudId;
	}
	
	public String getCloudId() {
		return cloudId;
	}
	
	public void setCloudId(String cloudId) {
		this.cloudId = cloudId;
	}
	public String getGraphs() {
		return graphs;
	}
	public void setGraphs(String graphs) {
		this.graphs = graphs;
	}

}
