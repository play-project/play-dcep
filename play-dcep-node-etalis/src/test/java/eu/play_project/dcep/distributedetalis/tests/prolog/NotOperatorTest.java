package eu.play_project.dcep.distributedetalis.tests.prolog;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.jtalis.core.JtalisContext;
import com.jtalis.core.JtalisContextImpl;
import com.jtalis.core.event.AbstractJtalisEventProvider;
import com.jtalis.core.event.EtalisEvent;
import com.jtalis.core.plengine.JPLEngineWrapper;
import com.jtalis.core.plengine.PrologEngineWrapper;

public class NotOperatorTest extends PrologAbstractTest{
	
	@Test
	public void testCnotOperator() throws InterruptedException {
		try {
			long delay = 100;
			final List<EtalisEvent> list = new LinkedList<EtalisEvent>();

			context.addEventTrigger("c");

			context.registerOutputProvider(new AbstractJtalisEventProvider() {
				@Override
				public void outputEvent(EtalisEvent event) {
					list.add(event);
				}
			});
		    	 
			context.addDynamicRule("c <- (a 'SEQ' b) 'cnot' (d seq e)");
			
			// Event d appears after seq.
			context.pushEvent(new EtalisEvent("a"));
			context.pushEvent(new EtalisEvent("b"));
			context.pushEvent(new EtalisEvent("d"));
			Thread.sleep(delay);
			Assert.assertEquals(1, list.size());
			
			// d seq e appeared. For this reason no 'c' was produced.
			context.pushEvent(new EtalisEvent("a"));
			context.pushEvent(new EtalisEvent("d"));
			context.pushEvent(new EtalisEvent("e"));
			context.pushEvent(new EtalisEvent("b"));
			Thread.sleep(delay);
			Assert.assertEquals(1, list.size());
			
			// Event 'd' appeared. But not the while seq.
			context.pushEvent(new EtalisEvent("a"));
			context.pushEvent(new EtalisEvent("d"));
			context.pushEvent(new EtalisEvent("b"));
			Thread.sleep(delay);
			Assert.assertEquals(2, list.size());
			
			// No 'b' appeared. For this reason 'c' was produced.
			context.pushEvent(new EtalisEvent("a"));
			context.pushEvent(new EtalisEvent("b"));
			Thread.sleep(delay);
			Assert.assertEquals(3, list.size());
			
			// No 'd' appeared before seq. For this reason no 'c' was produced.
			context.pushEvent(new EtalisEvent("d"));
			context.pushEvent(new EtalisEvent("e"));
			context.pushEvent(new EtalisEvent("a"));
			context.pushEvent(new EtalisEvent("b"));
			Thread.sleep(delay);
			Assert.assertEquals(4, list.size());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
