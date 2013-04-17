package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

public class ComplexEventSubscriber implements SimplePublishApi, Serializable{

	MeasurementUnit mu; 
	
	public ComplexEventSubscriber(){}
	
	@Override
	public void publish(CompoundEvent event) {
		if(mu==null){
			mu = new MeasurementUnit();
			mu.calcRateForNEvents(300);
		}
		//mu.nexEvent();
		printTimeSpentInSystem(event);
	}
	int eventCounter = 0;
	long eventTime;
	public void printTimeSpentInSystem(CompoundEvent event) {
		if ((eventCounter++ % 500) == 0) {
			for (Quadruple quadruple : event) {
				// Use endTime
				if (quadruple.getPredicate().toString().equals("http://events.event-processing.org/types/sedTime")) {
					Long.parseLong(quadruple.getObject().toString());
				}

				long time = System.currentTimeMillis();
				// Print time spend in system.
				System.out.println(time + "\t"
						+ (System.currentTimeMillis() - eventTime));
			}
		}
	}
}

