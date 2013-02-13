package eu.play_project.dcep.distributedetalis;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jpl.PrologException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.event.EtalisEvent;
import com.jtalis.core.event.JtalisInputEventProvider;

import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import fr.inria.eventcloud.api.CompoundEvent;

/**
 * To communicate with ETALIS. Push events to and receive events from ETALIS.
 * 
 * @author sobermeier
 */
public class JtalisInputProvider implements JtalisInputEventProvider,
		Serializable {

	private static final long serialVersionUID = 1L;
	BlockingQueue<EtalisEvent> events = null; // Contains events till ETALIS has consumed them.
	BlockingQueue<EtalisEvent> meausrementEvents = null; //Contains measurement events. They are preferred to the other events.
	boolean shutdownEtalis = false; // If true ETALIS will shutdown.
	private MeasurementUnit measurementUnit;
	private PrologSemWebLib semWebLib;
	public static int eventConsumed = 0;
	private static Logger logger = LoggerFactory.getLogger(JtalisInputProvider.class);

	public JtalisInputProvider(PrologSemWebLib semWebLib) {
		super();
		this.semWebLib = semWebLib;
		this.events = new LinkedBlockingQueue<EtalisEvent>();
	}

	/**
	 * Push events to Jtalis.
	 */
	public void notify(CompoundEvent event) {
		logger.info("DCEP Entry " + event.getGraph());
		try {
			semWebLib.addEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String eventType = EventCloudHelpers.getEventType(event);
		String eventId = event.getGraph().toString();
		
		try {
			System.out.println("New simple event: " + "'" + eventType + "'");
			events.put(new EtalisEvent("'" + eventType + "'", eventId));
		} catch (PrologException e) {
			logger.error("Error on new event. ", e);
		} catch (InterruptedException e) {
			logger.error("Error adding event to Jtalis queue.", e);
		}
	}

	@Override
	public boolean hasMore() {
		if (shutdownEtalis) {
			return false;
		} else {
			return true; // Otherwise workerthread of jtalis shuts down.
		}
	}
	
	/**
	 * Jtalis gets events from here.
	 */
	@Override
	public EtalisEvent getEvent() {
		
		// Return the oldest event (FIFO) blocking if there is none:
		try {
				EtalisEvent e = events.take();
				incrementEventCounter();
				return e;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void setup() {

	}
	
	public static synchronized void incrementEventCounter(){
		eventConsumed++;
	}
	
	public static synchronized void resetEventCounter(){
		eventConsumed = 0;
	}
	
	public static synchronized int getEventCounter(){
		return eventConsumed;
	}
	
	@Override
	public void shutdown() {
		shutdownEtalis = true;
	}
	
	public int getInputQueueSize(){
		return events.size();
	}
}
