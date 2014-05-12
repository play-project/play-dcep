package eu.play_project.dcep.distributedetalis.tests.prolog;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.jtalis.core.JtalisContext;
import com.jtalis.core.JtalisContextImpl;
import com.jtalis.core.event.AbstractJtalisEventProvider;
import com.jtalis.core.event.EtalisEvent;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.configurations.helpers.LoadPrologCode;

public class NotOperatorTriggerTest {
	
	@Test
	public void testEventTrigger() throws InterruptedException {
		try {
			long delay = 100;
			final List<EtalisEvent> complexEventList = new LinkedList<EtalisEvent>();

			PlayJplEngineWrapper engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
			JtalisContext context = new JtalisContextImpl(engine);
			context.addEventTrigger("c");

			context.registerOutputProvider(new AbstractJtalisEventProvider() {
				@Override
				public void outputEvent(EtalisEvent event) {
					complexEventList.add(event);
				}
			});
			
			// Load prolog libs.
			LoadPrologCode cl = new LoadPrologCode();
			cl.loadCode("TimeTrigger.pl", engine);
		    	 
			context.addDynamicRule("c <- (a seq b) 'cnot' d");
			
			// Fulfill pattern.
			context.pushEvent(new EtalisEvent("a"));
			context.pushEvent(new EtalisEvent("b"));
			Thread.sleep(delay);
			Assert.assertEquals(1, complexEventList.size());
			
			// Event b appears  in the period between event a and b.
			context.pushEvent(new EtalisEvent("a"));
			// Event d will be sent after one second.
			engine.executeGoal("triggerEventWithDelay(d, 1)");
			Thread.sleep(1020);
			engine.executeGoal("write('')");
			context.pushEvent(new EtalisEvent("b"));
			Thread.sleep(delay);
			
			Assert.assertEquals(1, complexEventList.size());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}