package eu.play_project.dcep.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Syntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.dcep.tests.SimplePublishApiSubscriber;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import fr.inria.eventcloud.api.CompoundEvent;


public class NotOperatorTimeBasedTest extends ScenarioAbstractTest {
	private final Logger logger = LoggerFactory.getLogger(ScenarioIntelligentTransportTest.class);
	
	@Test
	public void runTest() throws IOException, QueryDispatchException, InterruptedException {
		String queryString;

		// Get query.
		queryString = loadSparqlQuery("patterns/BDPL-Query-NotOperatorTime.eprq");
		logger.debug("BDPL query: \n{}", queryString);
		// Compile query
		queryDispatchApi.registerQuery("example1", queryString);

		//Subscribe to get complex events.
		SimplePublishApiSubscriber subscriber = null;
		try {
			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}

		// No DeliveryBid appeared.
		testApi.attach(subscriber);
		logger.info("Publish events");
		testApi.publish(EventCloudHelpers.toCompoundEvent(loadEvent("events/BidRequest.trig", Syntax.Trig)));
		logger.debug("Sent event: \n{}", EventCloudHelpers.toCompoundEvent(loadEvent("events/BidRequest.trig", Syntax.Trig)));

		//delay();


		// Wait
		//delay();
		//assertEquals("We expect exactly one complex event as a result.", 1, subscriber.getComplexEvents().size());
		
		//DeliveryBid appeared before transaction ends.
		logger.info("Publish events");
		
		CompoundEvent event = EventCloudHelpers.toCompoundEvent(loadEvent("events/BidRequest.trig", Syntax.Trig));
		for (int j = 0; j < 2; j++) {
			System.out.println(j);
			Thread.sleep(500);
			testApi.publish(event);

		}
//		testApi.publish(EventCloudHelpers.toCompoundEvent(loadEvent("events/BidRequest.trig", Syntax.Trig)));
//		logger.debug("Sent event: \n{}", EventCloudHelpers.toCompoundEvent(loadEvent("events/BidRequest.trig", Syntax.Trig)));
//		testApi.publish(EventCloudHelpers.toCompoundEvent(loadEvent("events/DeliveryBid.trig", Syntax.Trig)));
//		logger.debug("Sent event: \n{}", EventCloudHelpers.toCompoundEvent(loadEvent("events/DeliveryBid.trig", Syntax.Trig)));
//		testApi.publish(EventCloudHelpers.toCompoundEvent(loadEvent("events/TimeOut.trig", Syntax.Trig)));
//		logger.debug("Sent event: \n{}", EventCloudHelpers.toCompoundEvent(loadEvent("events/TimeOut.trig", Syntax.Trig)));
		
		// Wait
		delay();
		assertEquals("We expect exactly one complex event as a result.", 1, subscriber.getComplexEvents().size());
	}
	
	private void delay(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ModelRuntimeException, IOException {
		System.out.println(EventCloudHelpers.toCompoundEvent(loadEvent("events/ScenarioIntelligentTransportTest_Matlab.trig", Syntax.Trig)));
	}

}

