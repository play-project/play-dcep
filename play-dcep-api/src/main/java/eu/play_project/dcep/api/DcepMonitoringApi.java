package eu.play_project.dcep.api;

import com.hp.hpl.jena.query.Query;

import eu.play_project.dcep.api.measurement.NodeMeasuringResult;

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
	public void measurePerformance(Query measurementQuery, int measuringPeriod);
	public NodeMeasuringResult getMeasuredData(String queryId);
}
