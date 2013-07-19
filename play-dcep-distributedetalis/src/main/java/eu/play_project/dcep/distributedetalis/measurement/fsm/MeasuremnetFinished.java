package eu.play_project.dcep.distributedetalis.measurement.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import fr.inria.eventcloud.api.CompoundEvent;

public class MeasuremnetFinished implements MeasurementState {
	private MeasurementUnit context;
	private Logger logger;
	
	public MeasuremnetFinished(MeasurementUnit context) {
		this.context = context;
		this.logger = LoggerFactory.getLogger(MeasuremnetFinished.class);
	}

	@Override
	public void eventReceived() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NodeMeasurementResult getMeasuringResults() {
		logger.debug("Get measured data. Finish");
		NodeMeasurementResult  measuredValues = context.getMeasurementData();

		logger.info(measuredValues.getName());
		context.setState(context.createMeasurementState("Start"));

		return measuredValues;
	}

	@Override
	public void startMeasurement(int time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMeasuredData(NodeMeasurementResult measuredValues) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventProduced(CompoundEvent event, String patternId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "MeasurementFinshed";
	}

	@Override
	public void sendMeasuringEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void measuringPeriodIsUp() {
		// TODO Auto-generated method stub
		
	}

}
