package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.util.LinkedList;

import com.hp.hpl.jena.graph.NodeFactory;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Generate events and push them to the DistributedEtalisTestApi. The event rate
 * can be adapted. An instance of this class runs in his own thread. For this
 * reason the possible delay between events do not stops other threads.
 * 
 * @author sobermeier
 * 
 */
public class EventProducerThread implements Runnable {
	private final Thread thisThread;
	private final DistributedEtalisTestApi[] testApi;
	private final MeasurementUnit meausrementUnit;
	private final int numberOfEvents;
	private int delay;

	/**
	 * Generate an instance in his own thread and publish events to destinations.
	 * 
	 * @param testApi
	 *            Destination for the events.
	 * @param delay
	 *            Delay between two events. Given in ms.
	 */
	public EventProducerThread(int numberOfEvents, int delay, DistributedEtalisTestApi... testApi) {
		this.testApi = testApi;
		this.numberOfEvents = numberOfEvents;
		this.delay =  delay;
		
		meausrementUnit = MeasurementUnit.getMeasurementUnit();
		meausrementUnit.calcRateForNEvents(500);
		
		thisThread = new Thread(this);
		thisThread.start();
	}

	@Override
	public void run() {
		int destination = 0; //Destinations are stored in testApi.
		int count =0;

		// Publis event
		for (int i = 0; i < (numberOfEvents / testApi.length); i++) {

			// Distibute events in Round-robin fashon to all CEP-Engines.
			for (int j = 0; j < testApi.length; j++) {
				testApi[j].publish(createEvent("http://example.com/eventId/" + i));
				
				// Some statistics
				meausrementUnit.nexEvent();

				// Decrease delay
				count++;
				if (count % 2500 == 0) {
					if ((delay - 2) > 0) {
						delay -= 2;
					}
				}
				
				// Wait
				delay();
			}

		}
	}

	private void delay() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	public static CompoundEvent createEvent(String eventId) {

		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();

		Quadruple q1 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"	+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://events.event-processing.org/types/stream"),
				NodeFactory.createURI("http://streams.event-processing.org/ids/Srbench#stream"));

		Quadruple q3 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"	+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#WindSpeedObservation"));

		Quadruple q4 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#observedProperty"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#_WindSpeed"));

		Quadruple q5 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"));

		Quadruple q6 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
				NodeFactory.createURI("5.0"));

//		Quadruple q7 = new Quadruple(
//				NodeFactory.createURI("http://events.event-processing.org/eventId/"
//						+ eventId),
//				NodeFactory.createURI("http://prefix.example.com/e1"),
//				NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
//				NodeFactory.createURI(new SimpleDateFormat(
//						eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601)
//						.format(new Date())));
		Quadruple q7 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
				NodeFactory.createURI(System.currentTimeMillis() + ""));

		Quadruple q8 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"	+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#procedure"),
				NodeFactory.createURI("http://sensor.example.com/S1"));

		quads.add(q1);
		// quads.add(q2);
		quads.add(q3);
		quads.add(q4);
		quads.add(q5);
		quads.add(q6);
		quads.add(q7);
		quads.add(q8);

		return new CompoundEvent(quads);
	}
}
