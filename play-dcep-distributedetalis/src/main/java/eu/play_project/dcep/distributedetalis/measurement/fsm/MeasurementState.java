package eu.play_project.dcep.distributedetalis.measurement.fsm;

import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import fr.inria.eventcloud.api.CompoundEvent;

public interface MeasurementState {
	public void startMeasurement(int period);
	public void eventReceived();
	public void eventProduced(CompoundEvent event, String patternId);
	public void sendMeasuringEvent();
	public NodeMeasuringResult getMeasuringResults();
	public void setMeasuredData(NodeMeasuringResult measuredValues);
	public void measuringPeriodIsUp();
	public String getName();

}
