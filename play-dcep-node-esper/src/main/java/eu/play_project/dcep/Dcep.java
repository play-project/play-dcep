package eu.play_project.dcep;

import java.util.Map;

import org.event_processing.events.types.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.DcepApi;
import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.SimplePublishApi;
import eu.play_project.dcep.api.measurement.MeasurementConfig;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.distributedesper.DistributedEsper;
import eu.play_project.play_platformservices.PlayPlatformservices;
import eu.play_project.play_platformservices.api.BdplQuery;

public class Dcep implements DcepApi<Event> {
	
	private final Logger logger = LoggerFactory.getLogger(Dcep.class);
	private final DcepManagmentApi dcepManager;
	private final DistributedEsper distributedEsper;
	private final PlayPlatformservices platformServices;
	
	public Dcep() {
		
		this.distributedEsper = new DistributedEsper();
		this.dcepManager = distributedEsper; // Using many nodes, this implementation must become more complex
		
		this.platformServices = new PlayPlatformservices();
		this.platformServices.setDcepManagmentApi(this);
		this.platformServices.initialize();
	}

	@Override
	public void registerEventPattern(BdplQuery bdplQuery) throws DcepManagementException {
		logger.debug("Pattern reached DCEP facade: "
				+ bdplQuery.getDetails().getQueryId());
		
		dcepManager.registerEventPattern(bdplQuery);
	}

	@Override
	public void unregisterEventPattern(String queryId) {
		dcepManager.unregisterEventPattern(queryId);
	}

	@Override
	public BdplQuery getRegisteredEventPattern(String queryId) throws DcepManagementException {
		return dcepManager.getRegisteredEventPattern(queryId);
	}

	@Override
	public Map<String, BdplQuery> getRegisteredEventPatterns() {
		return dcepManager.getRegisteredEventPatterns();
	}

	@Override
	public void attach(SimplePublishApi<Event> subscriber) {
		this.distributedEsper.attach(subscriber);
	}

	@Override
	public void detach(SimplePublishApi<Event> subscriber) {
		this.distributedEsper.detach(subscriber);
		
	}

	@Override
	public void publish(Event event) {
		this.distributedEsper.publish(event);
	}

	@Override
	public void measurePerformance(MeasurementConfig config) {
		// no op
	}

	@Override
	public NodeMeasurementResult getMeasuredData(String queryId) {
		// no op
		return null;
	}



}
