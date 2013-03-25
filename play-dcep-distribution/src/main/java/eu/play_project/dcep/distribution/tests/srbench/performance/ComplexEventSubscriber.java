package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;

import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

public class ComplexEventSubscriber implements SimplePublishApi, Serializable{

	public ComplexEventSubscriber(){}

	@Override
	public void publish(CompoundEvent event) {
		//TODO stuehmer:  Implemt simulation.
	}
}
