package eu.play_project.dcep.distributedetalis.tests.prolog;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.jtalis.core.JtalisContext;
import com.jtalis.core.JtalisContextImpl;
import com.jtalis.core.event.AbstractJtalisEventProvider;
import com.jtalis.core.event.EtalisEvent;
import com.jtalis.core.plengine.JPLEngineWrapper;
import com.jtalis.core.plengine.PrologEngineWrapper;

import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.configurations.helpers.LoadPrologCode;

public class DelayEventTest extends PrologAbstractTest {
	
	/**
	 *  Fire event with delay of one second.
	 * @throws DistributedEtalisException 
	 * @throws IOException 
	 */
	@Test
	public void testTimeTrigger() throws InterruptedException, IOException, DistributedEtalisException {
			long delay = 3000;
			final List<EtalisEvent> list = new LinkedList<EtalisEvent>();

			context.addEventTrigger("c");

			context.registerOutputProvider(new AbstractJtalisEventProvider() {
				@Override
				public void outputEvent(EtalisEvent event) {
					list.add(event);
				}
			});

			LoadPrologCode loadPrologCode = new LoadPrologCode();
			loadPrologCode.loadCode("TimeTrigger.pl", engine);
			

			context.addDynamicRule("c <- a");
			
			engine.executeGoal("triggerEventWithDelay(a, 1)");
			engine.executeGoal("triggerEventWithDelay(a, 1)");
			engine.executeGoal("triggerEventWithDelay(a, 1)");
			engine.executeGoal("event(a)");
		    		    
			Thread.sleep(delay); // wait a little bit for the events to be processed
			engine.executeGoal("write('Print')");
			assertEquals("Exactly 4 events expected.", 4, list.size());
	}

}