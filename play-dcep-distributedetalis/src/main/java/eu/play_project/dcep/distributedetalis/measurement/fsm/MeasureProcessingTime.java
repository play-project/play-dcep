package eu.play_project.dcep.distributedetalis.measurement.fsm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import eu.play_project.dcep.distributedetalis.DistributedEtalis;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class MeasureProcessingTime implements MeasurementState{
	private MeasurementUnit context;
	private PrologSemWebLib semWebLib;
	private DistributedEtalis cepEngine;
	private Logger logger;
	private int measurementEventCounter = 0;
	private int complexEventCounter = 0;

	public MeasureProcessingTime( MeasurementUnit context, DistributedEtalis cepEngine, PrologSemWebLib semWebLib){
		this.cepEngine = cepEngine;
		this.semWebLib = semWebLib;
		this.context = context;
		this.logger = LoggerFactory.getLogger(MeasureProcessingTime.class);
	}

	@Override
	public void sendMeasuringEvent() {
		int mEvents = MeasurementUnit.mEvents; // Send 10 measuring events.
		logger.info("Send measurement events: " + mEvents );
		
		// Generate m events with current time.
		long currentTime = System.nanoTime();
		for (int i = 0; i < mEvents; i++) {
			CompoundEvent mEvent = generateMeasuringEvent(i, currentTime);
			
//			try {
//				semWebLib.addEvent(mEvent);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			cepEngine.notify(this.getSimpleEventType(mEvent), mEvent.getGraph().toString());
			// FIXME sobermeier: is it OK to use the facade method? it calls measuremtUnit.eventReceived()... again
			cepEngine.publish(mEvent);
		}
	}
	
	@Override
	public void eventProduced(CompoundEvent event, String patternId) {
		// Count events.
		if(patternId.equals("measurement-pattern")){
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
		logger.info(measurementEventCounter + "Measurements received. " + MeasurementUnit.mEvents * eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit.eventsPeriod + "Events expected.");
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
	public NodeMeasuringResult getMeasuringResults() {
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
                Node.createURI("http://play-project.eu/measurement/" + measurementID),
                Node.createURI("http://play-project.eu/measurement/event"),
                Node.createURI("http://play-project.eu/startTime"),
                Node.createURI(time + ""));
				
		Quadruple q2 = new Quadruple(
                Node.createURI("http://play-project.eu/measurement/" + measurementID),
                Node.createURI("http://play-project.eu/measurement/event"),
                RDF.type.asNode(),
                Node.createURI("measurementEvent"));
		quads.add(q1);
		quads.add(q2);
		
		return new CompoundEvent(quads);
	}
	
	@Override
	public void setMeasuredData(NodeMeasuringResult measuredValues) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "MeasureProcessingTime";
	}

	private long calcTimeForEvent(CompoundEvent event) {
		long singleEventTime = 0;

		// Calc processint time for one event
		Long currentTime = System.nanoTime();
		for (Quadruple quadruple : event.getQuadruples()) {
			if (quadruple.getPredicate().toString().equals("http://play-project.eu/timeOneEvent")) {
				singleEventTime = currentTime - Long.valueOf(quadruple.getObject().toString());
				singleEventTime = (singleEventTime/ MeasurementUnit.mEvents);
				logger.info("Time for" + MeasurementUnit.mEvents + " events ------------------------" + singleEventTime);
			}
		}
		return singleEventTime;
	}
}
