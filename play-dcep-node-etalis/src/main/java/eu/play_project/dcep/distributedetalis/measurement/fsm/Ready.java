package eu.play_project.dcep.distributedetalis.measurement.fsm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.measurement.MeasurementResult;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementThread;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import eu.play_project.dcep.distributedetalis.measurement.MeasuringThreadFactory;
import fr.inria.eventcloud.api.CompoundEvent;

public class Ready implements MeasurementState{

	private ExecutorService measureExecutor;
	private PlayJplEngineWrapper  prologContext;
	private MeasurementUnit context;
	private Logger logger;
	
	public Ready(PlayJplEngineWrapper  ctx, MeasurementUnit context){
		// Generate ThreadPool for Measurement tasks.
		measureExecutor = Executors.newCachedThreadPool();

		measureExecutor = Executors.newSingleThreadExecutor(new MeasuringThreadFactory());
		
		this.context = context;
		this.prologContext = ctx;
		this.logger = LoggerFactory.getLogger(Ready.class);
	}
	
	@Override
	public void startMeasurement(int time) {
		logger.info("Start measuremnt.");

		MeasurementThread task = new MeasurementThread(time, prologContext, context);

		Future<MeasurementResult> future = measureExecutor.submit(task);

		context.setState(context.createMeasurementState("MeasureProcessingTime"));
	}

	@Override
	public void eventReceived() {
		//Reflexive edge.
	}


	@Override
	public NodeMeasurementResult getMeasuringResults() {
		return null;
		//Reflexive edge.
	}


	@Override
	public void setMeasuredData(NodeMeasurementResult measuredValues) {
	}

	@Override
	public void eventProduced(CompoundEvent event, String patternId) {
	}

	@Override
	public String getName() {
		return "Ready";
	}

	@Override
	public void sendMeasuringEvent() {
	}

	@Override
	public void measuringPeriodIsUp() {
	}

}
