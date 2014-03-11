package eu.play_project.dcep.distributedetalis.measurement.fsm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.play_project.dcep.api.measurement.MeasurementConstants;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.distributedetalis.DistributedEtalis;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class MeasureProcessingTime implements MeasurementState{
	private final MeasurementUnit context;
	private final PrologSemWebLib semWebLib;
	private final DistributedEtalis cepEngine;
	private final Logger logger;
	private int measurementEventCounter = 0;
	private int complexEventCounter = 0;
	private int measurementCounter = 0; // Count how often events were sent.

	public MeasureProcessingTime( MeasurementUnit context, DistributedEtalis cepEngine, PrologSemWebLib semWebLib){
		this.cepEngine = cepEngine;
		this.semWebLib = semWebLib;
		this.context = context;
		this.logger = LoggerFactory.getLogger(MeasureProcessingTime.class);
	}

	@Override
	public void sendMeasuringEvent() {
		int mEvents = MeasurementUnit.mEvents; // Send 10 measuring events.
		logger.info("Send measurement events: {}", mEvents );
		
		// Generate m events with current time.
		long currentTime = System.nanoTime();
		measurementCounter++;
		for (int i = 0; i < mEvents; i++) {
			CompoundEvent mEvent = generateMeasuringEvent((i + measurementCounter), currentTime);
			
			// Skip ProActive queue.
			try {
				semWebLib.addEvent(mEvent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//cepEngine.notify(this.getSimpleEventType(mEvent), mEvent.getGraph().toString());
		
			cepEngine.publish(mEvent);
		}
	}
	
	@Override
	public void eventProduced(CompoundEvent event, String type) {
		// Count events.
		if(type.equals("org/types/ComplexMeasurementEvent'")){
			logger.debug("New complex m event received. ");
			measurementEventCounter++;
			//Use time of MeasurementUnit.mEvents
			if(measurementEventCounter%MeasurementUnit.mEvents ==0){
				context.addSingleEventTime(calcTimeForEvent(event));
			}
			//Check if all measurements are received.
			if(measurementEventCounter==(MeasurementUnit.mEvents * eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit.eventsPeriod)){
				
				
				//context.setNumberOfInputEvents(context.getNumberOfInputEvents() + numberOfConsumedEvents);
			}
		}else{
			complexEventCounter++;
		}
	}
	
	@Override
	public void measuringPeriodIsUp() {
		logger.debug("Switch state");
		logger.info("{} Measurements received. {} Events expected.", measurementEventCounter, MeasurementUnit.mEvents * MeasurementUnit.eventsPeriod);
		if(measurementEventCounter>=((eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit.eventsPeriod)-1)){
			MeasurementState s =  new WaitForMeasuredData(context);
			context.setState(s);
			//Set number of consumed and produced events.
			context.setNumberOfOutputEvents(context.getNumberOfOutputEvents()+complexEventCounter);
		}else {
			MeasurementState s =  new WaitForComplexMeasurementEvent(context, complexEventCounter);
			context.setState(s);
		}
	}
	
	
	@Override
	public void eventReceived() {
		context.setNumberOfInputEvents(1); //Count first event after startup.
				
		// Set next state.
		//this.context.setState(context.create("WaitForComplexMeasurementEvents"));
	}



	@Override
	public NodeMeasurementResult getMeasuringResults() {
		return null;
		//Reflexive edge.
	}

	@Override
	public void startMeasurement(int period) {
		//Reflexive edge.
	}

	private CompoundEvent generateMeasuringEvent(int measurementID, long time){
		//Measure time for one event.
		List<Quadruple> quads = new ArrayList<Quadruple>();
				
		Quadruple q1 = new Quadruple(
				NodeFactory.createURI("http://play-project.eu/measurement/" + measurementID),
				NodeFactory.createURI("http://play-project.eu/measurement/event"),
                RDF.type.asNode(),
                NodeFactory.createURI(MeasurementConstants.MEASUREMENT_SIMPLE_TYPE)
                );
		Quadruple q2 = new Quadruple(
				NodeFactory.createURI("http://play-project.eu/measurement/" + measurementID),
				NodeFactory.createURI("http://play-project.eu/measurement/event"),
                NodeFactory.createURI("http://events.event-processing.org/types/stream"),
                NodeFactory.createURI("http://streams.event-processing.org/ids/Local#stream")
                );
		
		Quadruple q3 = new Quadruple(
				NodeFactory.createURI("http://play-project.eu/measurement/" + measurementID),
				NodeFactory.createURI("http://play-project.eu/measurement/event"),
				NodeFactory.createURI("http://events.event-processing.org/types/sendTime"),
				NodeFactory.createURI(time + "")
				);
		
		Quadruple q4 = new Quadruple(
				NodeFactory.createURI("http://play-project.eu/measurement/" + measurementID),
				NodeFactory.createURI("http://play-project.eu/measurement/event"),
				NodeFactory.createURI("http://events.event-processing.org/types/payload"),
				NodeFactory.createURI("payload")
				);
		
		
		quads.add(q1);
		quads.add(q2);
		quads.add(q3);
		quads.add(q4);
		
		return new CompoundEvent(quads);
	}
	
	@Override
	public void setMeasuredData(NodeMeasurementResult measuredValues) {
	}

	@Override
	public String getName() {
		return "MeasureProcessingTime";
	}

	private long calcTimeForEvent(CompoundEvent event) {
		long singleEventTime = 0;

		// Calc processint time for one event
		Long currentTime = System.nanoTime();
		for (Quadruple quadruple : event) {
			if (quadruple.getPredicate().toString().equals("http://play-project.eu/timeOneEvent")) {
				singleEventTime = currentTime - Long.valueOf(quadruple.getObject().toString());
				singleEventTime = (singleEventTime/ MeasurementUnit.mEvents);
				logger.info("Time for {} events ------------------------ {}", MeasurementUnit.mEvents, singleEventTime);
			}
		}
		return singleEventTime;
	}
}
