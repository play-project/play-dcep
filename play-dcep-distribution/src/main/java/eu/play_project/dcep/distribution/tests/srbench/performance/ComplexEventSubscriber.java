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
			mu.calcRateForNEvents(2);
		}
		//mu.nexEvent();
		printTimeSpentInSystem(event);
	}
	
	public void printTimeSpentInSystem(CompoundEvent event) {

		for (Quadruple quadruple : event) {
			// Use endTime
			if (quadruple.getPredicate().toString().equals("http://events.event-processing.org/types/sedTime")) {

				// Get time and pars it.
				SimpleDateFormat sdf = new SimpleDateFormat(
						eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601);
				Date date = null;
				try {
					date = sdf
							.parse(quadruple.getObject().toString().replace("\"", "").replace("^^http://www.w3.org/2001/XMLSchema#dateTime",""));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				long time = System.currentTimeMillis();
				// Print time pent in system.
				System.out.println(time + "\t" + (System.currentTimeMillis() - date.getTime()));
			}
		}
	}
}
