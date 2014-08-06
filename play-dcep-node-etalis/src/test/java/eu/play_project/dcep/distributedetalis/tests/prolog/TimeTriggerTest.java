package eu.play_project.dcep.distributedetalis.tests.prolog;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.jtalis.core.JtalisContext;
import com.jtalis.core.JtalisContextImpl;
import com.jtalis.core.event.AbstractJtalisEventProvider;
import com.jtalis.core.event.EtalisEvent;
import com.jtalis.core.plengine.JPLEngineWrapper;
import com.jtalis.core.plengine.PrologEngineWrapper;

public class TimeTriggerTest extends PrologAbstractTest{
	
	/**
	 *  Fire event with delay of one second. (With basic prolog methods)
	 */
	@Test
	public void testTimeTrigger() throws InterruptedException {
			long delay = 3000;
			final List<EtalisEvent> list = new LinkedList<EtalisEvent>();

			context.addEventTrigger("c");

			context.registerOutputProvider(new AbstractJtalisEventProvider() {
				@Override
				public void outputEvent(EtalisEvent event) {
					list.add(event);
				}
			});

			context.addDynamicRule("c <- a");
			
			engine.executeGoal("alarm(1 , event(a), _ID, [])");
			engine.executeGoal("alarm(1 , event(a), _ID, [])");
			engine.executeGoal("alarm(1 , event(a), _ID, [])");
			engine.executeGoal("event(a)");
		    		    
			Thread.sleep(delay); // wait a little bit for the events to be processed
			engine.executeGoal("write('Print')");
			assertEquals("Exactly 4 events expected.", 4, list.size());
	}

}
