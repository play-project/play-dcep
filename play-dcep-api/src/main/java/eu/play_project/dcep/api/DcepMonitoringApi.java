package eu.play_project.dcep.api;

import eu.play_project.dcep.api.measurement.NodeMeasuringResult;

/**
 * Manage the DCEP in non-functional ways such as configuring distribution strategies, constraining resources 
 * or querying monitoring information
 * @author sobermeier
 *
 */

public interface DcepMonitoringApi {

	// Measure data. Time in ms.
	public NodeMeasuringResult getMeasurementData();
	public void measurePerformance(int measuringPeriod);
}
