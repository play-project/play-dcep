package eu.play_project.dcep.distributedetalis.measurement;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.measurement.MeasuringResult;
import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import eu.play_project.dcep.api.measurement.PatternMeasuringResult;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;

public class MeasurementThread implements Callable<MeasuringResult> {
	private Logger logger;
	private int measuringPeriod = 0;
	private PlayJplEngineWrapper ctx; // CEP-Engine
	private MeasurementUnit mainProgramm;

	public MeasurementThread(int measuringPeriod, PlayJplEngineWrapper ctx, MeasurementUnit mainProgramm) {
		logger = LoggerFactory.getLogger(MeasurementThread.class);
		this.measuringPeriod = measuringPeriod;
		this.mainProgramm = mainProgramm;
		this.ctx = ctx;
	}

	@Override
	public NodeMeasuringResult call() throws Exception {

		logger.debug("Start new measurement peride with " + measuringPeriod + "ms");

		ctx.executeGoal("setMeasurementMode(on)");
		// Wait till measurement time is up. Send triger 5 measurements.
		int measureEvents = eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit.eventsPeriod ;// Number of measurement events in one period.
		
			int partMPeriod = (measuringPeriod/measureEvents);
			while(partMPeriod <= measuringPeriod){
				// Send measuring event.
				mainProgramm.sendMeasureEvents();
				
				// Note that event has ben sent.
				partMPeriod += measuringPeriod/measureEvents;

				// Wait 
				Thread.sleep((measuringPeriod/measureEvents));
			}
		
		// Stop measurement
		ctx.executeGoal("setMeasurementMode(off)");
		logger.debug("Measurement mode: off");
		
		// Next state
		mainProgramm.measuringPeriodIsUp();
	
		// Get measured data.
		logger.debug("Get measured data form prolog.");
		NodeMeasuringResult values = getMeasuredValues();

		// Cleanup
		logger.debug("Delete measured data..");
		ctx.executeGoal("deleteMeasuredData");
		
		//
		mainProgramm.setMeasuredValues(values);
		logger.info("Measurement periode is up.");
		
		// Return result.
		return values;
	}

	public NodeMeasuringResult getMeasuredValues() {
		logger.debug("getMeasuredValues");
		StringBuffer comand = new StringBuffer();
		List<PatternMeasuringResult> results = null;

		comand.append("eventCounter(PatternID,Value)");

		// Get patternIDs and values
		Hashtable<String, Object>[] resultFromProlog = ctx.execute((comand.toString()));

		// Create result object
		if (resultFromProlog != null) {
			results = new LinkedList();
			for (Hashtable<String, Object> hashtable : resultFromProlog) {
				logger.info(hashtable.get("PatternID").toString()  + "aaaaaaaa \t ssssssssssss" +((jpl.Integer) hashtable.get("Value")).intValue());
				// Put data in ResultSet.
				results.add(new PatternMeasuringResult(hashtable.get("PatternID").toString(), ((jpl.Integer) hashtable.get("Value")).intValue()));
			}
		}

		if (results == null) {
			logger.error("No measuring results");
		}

		return new NodeMeasuringResult("DummyETALIS-NodeName", measuringPeriod, results);
	}


}
