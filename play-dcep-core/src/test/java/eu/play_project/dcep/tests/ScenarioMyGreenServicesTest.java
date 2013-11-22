package eu.play_project.dcep.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.SimplePublishApiSubscriber;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;


public class ScenarioMyGreenServicesTest extends ScenarioAbstractTest {

	public static QueryDispatchApi queryDispatchApi;
	public static DistributedEtalisTestApi testApi;
	boolean start = false;
	static Component root;
	public static boolean test;
	private final Logger logger = LoggerFactory.getLogger(ScenarioMyGreenServicesTest.class);
	
	@Test
	public void testMyGreenServicesScenario() throws QueryDispatchException, IOException {
		
		String queryString;

		// Get query.
		queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("patterns/play-bdpl-inria-green-services-01.eprq"));

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
	
		String xmlEvent = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("events/MyGreenServicesSensors.nativeMessage.xml"));
		// TODO stuehmer fininsh test
		
		// Wait
		delay();

		assertTrue(subscriber.getComplexEvents().size()==0);
	}
	
	private void delay(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}