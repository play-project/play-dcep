package eu.play_project.dcep.scenarios;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.fractal.api.Component;
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

public class EventIdUriTest extends ScenarioAbstractTest {
	
	boolean start = false;
	static Component root;
	public static boolean test;
	private final Logger logger = LoggerFactory.getLogger(ScenarioIntelligentTransportTest.class);
	
	@Test
	public void testEentIdAsUri() throws QueryDispatchException, IOException {
		
		String queryString;

		// Get query.
		queryString = loadSparqlQuery("patterns/play-bdpl-telco-recommend-location.eprq");

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

		testApi.attach(subscriber);
		logger.info("Publish events");
		testApi.publish(EventCloudHelpers.toCompoundEvent(loadEvent("events/ScenarioTelcoLocationTest_Call.trig", Syntax.Trig)));
		// TODO stuehmer finish test
	

		// Wait
		delay();

		assertEquals("We expect exactly one complex event as a result.", 1, subscriber.getComplexEvents().size());
	}
	
	private void delay(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ModelRuntimeException, IOException {
		System.out.println(EventCloudHelpers.toCompoundEvent(loadEvent("events/ScenarioIntelligentTransportTest_Matlab.trig", Syntax.Trig)));
	}

}
