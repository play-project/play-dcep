package eu.play_project.dcep.distributedetalis.measurement.fsm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.measurement.MeasuringResult;
import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
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

		Future<MeasuringResult> future = measureExecutor.submit(task);

		context.setState(context.create("MeasureProcessingTime"));
	}

	@Override
	public void eventReceived() {
		//Reflexive edge.
	}


	@Override
	public NodeMeasuringResult getMeasuringResults() {
		return null;
		//Reflexive edge.
	}


	@Override
	public void setMeasuredData(NodeMeasuringResult measuredValues) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventProduced(CompoundEvent event, String patternId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Ready";
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
