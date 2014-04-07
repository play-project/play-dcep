package eu.play_project.dcep.distributedetalis.api;

import java.util.List;

import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.HistoricalQuery;

public interface HistoricalDataEngine {
	
	/**
	 * To get historical data for the given pattern and given predefined values for some variables.
	 * @param queries Select queries grouped by stream id.
	 * @param variableBindings Values for given variables.
	 * @return
	 */
	public HistoricalData get(List<HistoricalQuery> queries, VariableBindings variableBindings);

}
