package eu.play_project.dcep.distributedetalis.measurement.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;

import eu.play_project.dcep.api.measurement.MeasurementConstants;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import fr.inria.eventcloud.api.CompoundEvent;

public class WaitForComplexMeasurementEvent implements MeasurementState{
	private int measurementEventCounter = 0;
	private int complexEventCounter = 0; // All complex events except measurement events.
	private MeasurementUnit context;
	private int numberOfConsumedEvents;
	private Logger logger;

	public WaitForComplexMeasurementEvent(MeasurementUnit context, int measuementEventCounter) {
		this.context = context;
		this.logger = LoggerFactory.getLogger(WaitForComplexMeasurementEvent.class);
		this.measurementEventCounter = measuementEventCounter;
	}

	@Override
	public void eventProduced(CompoundEvent event, String type) {
		System.out.println("Fuck You: WW.................................................................");
		// Count events.
		if (type.equals(MeasurementConstants.MEASUREMENT_COMPLEX_TYPE)) {
			logger.debug("New complex m event received. ");
			measurementEventCounter++;
			context.addSingleEventTime(calcTimeForEvent(event));
			// Check if all measurements are received.
			if (measurementEventCounter == (MeasurementUnit.mEvents * eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit.eventsPeriod)) {

				// context.setNumberOfInputEvents(context.getNumberOfInputEvents()
				// + numberOfConsumedEvents);
			}
		} else {
			complexEventCounter++;
		}

		// Check if all measurements are received.
		if (measurementEventCounter == (MeasurementUnit.mEvents * eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit.eventsPeriod)) {

			// Set number of consumed and produced events.
			context.setNumberOfOutputEvents(context.getNumberOfOutputEvents() + complexEventCounter);
			context.setNumberOfInputEvents(context.getNumberOfInputEvents() + numberOfConsumedEvents);

			context.setState(context.createMeasurementState("WaitForMeasuredData"));
		}
	}

	@Override
	public NodeMeasurementResult getMeasuringResults() {
		return null;
	}

	@Override
	public void startMeasurement(int period) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventReceived() {
		numberOfConsumedEvents++;
	}
	
	@Override
	public void setMeasuredData(NodeMeasurementResult measuredValues) {
	}

	private long calcTimeForEvent(CompoundEvent event) {
		long singleEventTime = 0;

		// Calc processint time for one event
		Long currentTime = System.nanoTime();
		for (Triple quadruple : event.getTriples()) {
			if (quadruple.getPredicate().toString().equals("http://play-project.eu/timeOneEvent")) {
				singleEventTime = currentTime - Long.valueOf(quadruple.getObject().toString());
				singleEventTime = singleEventTime / MeasurementUnit.mEvents;
				logger.info("Time for" + MeasurementUnit.mEvents + " events ------------------------" + singleEventTime);
			}
		}

		return singleEventTime;
	}
	@Override
	public String getName() {
		return "WaitForComplexMeasurementEvents";
	}

	@Override
	public void sendMeasuringEvent() {
				
	}

	@Override
	public void measuringPeriodIsUp() {
		
	}
}
