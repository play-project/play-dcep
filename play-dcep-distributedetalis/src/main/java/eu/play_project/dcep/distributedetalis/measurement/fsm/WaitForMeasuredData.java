package eu.play_project.dcep.distributedetalis.measurement.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import fr.inria.eventcloud.api.CompoundEvent;

public class WaitForMeasuredData implements MeasurementState{

	private MeasurementUnit context;
	private Logger logger;
	private int numberOfProducedEvents;
	private int numberOfConsumedEvents;

	public WaitForMeasuredData(MeasurementUnit context){
		this.logger = LoggerFactory.getLogger(WaitForMeasuredData.class);
		this.context = context;
	}
	
	@Override
	public void eventReceived() {
		numberOfConsumedEvents++;
	}


	@Override
	public NodeMeasuringResult getMeasuringResults() {
		return null;
	}

	@Override
	public void startMeasurement(int period) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMeasuredData(NodeMeasuringResult measuredValues) {
		logger.debug("Measured data set.");
		logger.info("" + measuredValues.getProcessingTimeForOneEvent());
		logger.info("" + measuredValues.getNumberOfEtalisInputEvents());
		logger.info(measuredValues.getName());

		//Set number of consumed and produced events.
		context.setNumberOfOutputEvents(context.getNumberOfOutputEvents()+numberOfProducedEvents);
		context.setNumberOfInputEvents(context.getNumberOfInputEvents() + numberOfConsumedEvents);
		
		context.setState( context.create("MeasurementFinished"));
	}


	@Override
	public void eventProduced(CompoundEvent event, String patternId) {
		numberOfProducedEvents++;
	}
	@Override
	public String getName() {
		return "WaitForMeasuredData";
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
