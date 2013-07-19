package eu.play_project.dcep.api;

import eu.play_project.dcep.api.measurement.MeasurementConfig;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;

/**
 * Manage the DCEP in non-functional ways such as configuring distribution strategies, constraining resources 
 * or querying monitoring information
 * @author sobermeier
 *
 */

public interface DcepMonitoringApi {

	/**
	 * Measure performance with given query.
	 * @param measurementQuery
	 * @param measuringPeriod
	 */
	public void measurePerformance(MeasurementConfig config);
	public NodeMeasurementResult getMeasuredData(String queryId);
}
