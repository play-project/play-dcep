package eu.play_project.dcep.distributedetalis.api;

import java.util.List;
import java.util.Map;

import eu.play_project.play_platformservices.api.HistoricalQuery;

public interface HistoricalData {
	
	/**
	 * To get historical data for the given pattern and predefined values for some variables.
	 * @param queries Select queries grouped by stream id.
	 * @param variableBindings Values for given variables.
	 * @return
	 */
	public Map<String, List<String>> get(List<HistoricalQuery> queries, Map<String, List<String>> variableBindings);

}
