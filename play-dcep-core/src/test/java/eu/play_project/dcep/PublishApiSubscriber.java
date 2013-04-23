package eu.play_project.dcep;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import fr.inria.eventcloud.api.CompoundEvent;

public class PublishApiSubscriber implements SimplePublishApi, Serializable {

	private static final long serialVersionUID = 100L;
	ArrayList<CompoundEvent> complexEvents = new ArrayList<CompoundEvent>();
	Logger logger;


	public PublishApiSubscriber() {
		logger = LoggerFactory.getLogger(PublishApiSubscriber.class);
	}

	public ArrayList<CompoundEvent> getComplexEvents() {
		return complexEvents;
	}

	@Override
	public void publish(CompoundEvent arg0) {
		complexEvents.add(arg0);
		logger.info(complexEvents.size() + " Events received by subscriber.");
	}
}