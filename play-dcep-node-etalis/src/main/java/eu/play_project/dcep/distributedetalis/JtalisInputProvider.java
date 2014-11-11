package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_ENTRY;
import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.event.EtalisEvent;
import com.jtalis.core.event.JtalisInputEventProvider;

import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.dcep.node.api.DcepNodeException;
import fr.inria.eventcloud.api.CompoundEvent;

/**
 * To communicate with ETALIS. Push events to and receive events from ETALIS.
 * 
 * @author sobermeier
 */
public class JtalisInputProvider implements JtalisInputEventProvider,
		Serializable {

	private static final long serialVersionUID = 100L;
	BlockingQueue<EtalisEvent> events = null; // Contains events till ETALIS has consumed them.
	BlockingQueue<EtalisEvent> meausrementEvents = null; //Contains measurement events. They are preferred to the other events.
	boolean shutdownEtalis = false; // If true ETALIS will shutdown.
	private MeasurementUnit measurementUnit;
	private final PrologSemWebLib semWebLib;
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
		String eventType = EventCloudHelpers.getEventType(event);
		String eventId = event.getGraph().toString();

		try {
			//Thread.sleep(100);
			// Add RDF payload to Prolog:
			semWebLib.addEvent(event);
			// Trigger event in ETALIS:
			events.put(new EtalisEvent(quoteForProlog(eventType), eventId));
		} catch (InterruptedException e) {
			logger.error(LOG_DCEP_FAILED_ENTRY + "Error adding event to Jtalis queue.", e);
		} catch (DcepNodeException e) {
			logger.error(LOG_DCEP_FAILED_ENTRY + "Error on new event. ", e);
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
		while (true) {
			try {
				EtalisEvent e = events.take();
				incrementEventCounter();
				return e;
			} catch (InterruptedException e) {
				logger.debug("Thread '{}' got interrupted while taking an event from the queue.", Thread.currentThread());
			}
		}
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
