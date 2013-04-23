package eu.play_project.dcep.distribution.tests.single_pattern;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class ComplexEventSubscriber implements SimplePublishApi, Serializable{
	private static final long serialVersionUID = 100L;
	private int eventCounter = 0;

	public ComplexEventSubscriber(){}

	
	@Override
	public void publish(CompoundEvent event) {
		eventCounter++;
		//System.out.println("New complex Event " + eventCounter + ": " + event);
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

				// Print time pent in system.
				System.out.println((System.currentTimeMillis()- 3600000) - date.getTime());
			}
		}
	}
}
