package eu.play_project.dcep;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

public class PublishApiSubscriber implements SimplePublishApi, Serializable {

	private static final long serialVersionUID = 1L;
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
		//logger.info("New complex  event" + arg0 + "\nIn " + this.getClass().getSimpleName());
		complexEvents.add(arg0);
		logger.info(complexEvents.size() + " Events received by subscriber.");
	}
}