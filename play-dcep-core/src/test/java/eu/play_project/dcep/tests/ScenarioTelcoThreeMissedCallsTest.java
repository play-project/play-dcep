package eu.play_project.dcep.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ontoware.rdf2go.impl.jena.ModelImplJena;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

import eu.play_project.dcep.SimplePublishApiSubscriber;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;


public class ScenarioTelcoThreeMissedCallsTest extends ScenarioAbstractTest {

	public static QueryDispatchApi queryDispatchApi;
	public static DistributedEtalisTestApi testApi;
	boolean start = false;
	static Component root;
	public static boolean test;
	private final Logger logger = LoggerFactory.getLogger(ScenarioTelcoThreeMissedCallsTest.class);
	
	@Test
	public void testThreeMissedCalls() throws QueryDispatchException, IOException {
		
		String queryString;

		// Get query.
		queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("patterns/play-bdpl-telco-orange-eval-v3-full.eprq"));

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
		for (int i = 0; i < 5; i++) {
			Model call0 = RDFDataMgr.loadModel("events/call0.nq", RDFLanguages.NQ);
			testApi.publish(EventCloudHelpers.toCompoundEvent(new ModelImplJena(new URIImpl(call0.getGraph().toString()), call0)));
			Model call1 = RDFDataMgr.loadModel("events/call1.nq", RDFLanguages.NQ);
			testApi.publish(EventCloudHelpers.toCompoundEvent(new ModelImplJena(new URIImpl(call1.getGraph().toString()), call1)));
			Model call2 = RDFDataMgr.loadModel("events/call2.nq", RDFLanguages.NQ);
			testApi.publish(EventCloudHelpers.toCompoundEvent(new ModelImplJena(new URIImpl(call2.getGraph().toString()), call2)));
		}

		// Wait
		delay();

		assertTrue(subscriber.getComplexEvents().size()==1);
	}

	private void delay(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}