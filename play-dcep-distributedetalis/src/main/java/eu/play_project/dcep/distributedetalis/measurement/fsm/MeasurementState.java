package eu.play_project.dcep.distributedetalis.measurement.fsm;

import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import fr.inria.eventcloud.api.CompoundEvent;

public interface MeasurementState {
	public void startMeasurement(int period);
	public void eventReceived();
	public void eventProduced(CompoundEvent event, String patternId);
	public void sendMeasuringEvent();
	public NodeMeasurementResult getMeasuringResults();
	public void setMeasuredData(NodeMeasurementResult measuredValues);
	public void measuringPeriodIsUp();
	public String getName();

}
