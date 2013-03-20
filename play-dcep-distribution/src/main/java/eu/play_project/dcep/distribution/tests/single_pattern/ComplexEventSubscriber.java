package eu.play_project.dcep.distribution.tests.single_pattern;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;

import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

public class ComplexEventSubscriber implements SimplePublishApi, Serializable{
	private static final long serialVersionUID = 1L;
	private int eventCounter = 0;

	public ComplexEventSubscriber(){}

	
	@Override
	public void publish(CompoundEvent event) {
		eventCounter++;
		System.out.println("New complex Event " + eventCounter + ": " + event);
	}
}
