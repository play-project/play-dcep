package eu.play_project.dcep.distributedetalis.measurement;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.measurement.MeasurementResult;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.api.measurement.PatternMeasuringResult;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.node.api.DcepNodeException;

public class MeasurementThread implements Callable<MeasurementResult> {
	private final Logger logger;
	private int measuringPeriod = 0;
	private final PlayJplEngineWrapper ctx; // CEP-Engine
	private final MeasurementUnit mainProgramm;

	public MeasurementThread(int measuringPeriod, PlayJplEngineWrapper ctx, MeasurementUnit mainProgramm) {
		logger = LoggerFactory.getLogger(MeasurementThread.class);
		this.measuringPeriod = measuringPeriod;
		this.mainProgramm = mainProgramm;
		this.ctx = ctx;
	}

	@Override
	public NodeMeasurementResult call() throws Exception {

		logger.debug("Start new measurement peride with {}ms", measuringPeriod);

		ctx.executeGoal("setMeasurementMode(on)");
		
		// Wait till measurement time is up. Send triger 5 measurements.
		int measureEvents = eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit.eventsPeriod ;// Number of measurement events in one period.
		
			float partMPeriod = (measuringPeriod/measureEvents-10);
			while(partMPeriod <= measuringPeriod){
				// Send measuring event.
				mainProgramm.sendMeasureEvents();
				
				// Note that event has been sent.
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
		NodeMeasurementResult values = getMeasuredValues();

		// Cleanup
		logger.debug("Delete measured data.");
		ctx.executeGoal("deleteMeasuredData");

		//
		mainProgramm.setMeasuredValues(values);
		logger.info("Measurement periode is up.");

		// Return result.
		return values;
	}

	public NodeMeasurementResult getMeasuredValues() {
		logger.debug("getMeasuredValues");
		StringBuffer comand = new StringBuffer();
		Hashtable<String, Object>[] resultFromProlog = null;
		List<PatternMeasuringResult> results = null;

		comand.append("eventCounter(PatternID,Value)");

		// Get patternIDs and values
		
		try {
			resultFromProlog = ctx.execute(comand.toString());
		} catch (DcepNodeException e) {
			logger.warn("Problem occurred while getting eventCounter: {}", e.getMessage());
		}

		// Create result object
		if (resultFromProlog != null) {
			results = new LinkedList<PatternMeasuringResult>();
			for (Hashtable<String, Object> hashtable : resultFromProlog) {
				logger.info("Pattern '{}' consumed {} events.", hashtable.get("PatternID").toString(), ((jpl.Integer) hashtable.get("Value")).intValue());
				// Put data in ResultSet.
				results.add(new PatternMeasuringResult(hashtable.get("PatternID").toString(), ((jpl.Integer) hashtable.get("Value")).intValue()));
			}
		}

		if (results == null) {
			logger.error("No measuring results");
		}

		return new NodeMeasurementResult("DummyETALIS-NodeName", measuringPeriod, results);
	}


}
