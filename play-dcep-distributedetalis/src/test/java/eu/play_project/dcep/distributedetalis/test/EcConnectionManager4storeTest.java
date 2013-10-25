package eu.play_project.dcep.distributedetalis.test;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.apache.cxf.BusFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.event_processing.events.types.Event;
import org.event_processing.events.types.UcTelcoCall;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ow2.play.governance.platform.user.api.rest.PublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.DistributedEtalis;
import eu.play_project.dcep.distributedetalis.EcConnectionManager4store;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.platformservices.eventvalidation.InvalidEventException;
import eu.play_project.platformservices.eventvalidation.Validator;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import eu.play_project.play_eventadapter.AbstractSenderRest;
import eu.play_project.play_eventadapter.NoRdfEventException;
import fr.inria.eventcloud.api.CompoundEvent;

public class EcConnectionManager4storeTest {

	private static final String NOTIFY_URI = "http://localhost:10085";
	private static final String FOURSTORE_PATH = "/4store";
	private static final String FOURSTORE_URI = "http://localhost:10085" + "/4store";
	private static final List<Model> eventSink = Collections.synchronizedList(new ArrayList<Model>());
	private static final List<String> rdfSink = Collections.synchronizedList(new ArrayList<String>());
	private static Logger logger = LoggerFactory.getLogger(EcConnectionManager4storeTest.class);
	private static Server notifyReceiverRest;

	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		Application listener = new TestListenerRest(eventSink);
		Application fourstore = new TestFourstore(rdfSink);

		final ResourceConfig rc = new ResourceConfig()
				.register(listener)
				.register(fourstore)
				.register(MoxyJsonFeature.class);
		
		BusFactory.getDefaultBus(true);
		
		notifyReceiverRest = new Server(URI.create(NOTIFY_URI).getPort());
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder h = new ServletHolder(new ServletContainer(rc));
        context.addServlet(h, "/");
        notifyReceiverRest.setHandler(context);
        notifyReceiverRest.start();

        logger.info("Test server started.");
	}

	@Test
	public void testRestfulSendAndReceive() throws InvalidEventException {

		/*
		 * (1) Send event
		 */
		AbstractSenderRest rdfSender = new AbstractSenderRest("http://example.com/topic", NOTIFY_URI + PublishService.PATH);
		
		String eventId = EventHelpers.createRandomEventId("UnitTest");
		UcTelcoCall event = new UcTelcoCall(EventHelpers.createEmptyModel(eventId),
			eventId + EVENT_ID_SUFFIX, true);
		event.setEndTime(Calendar.getInstance());
		event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));
		
		rdfSender.notify(event);
		
        /*
         * (2) Wait for the event to be received:
         */
   		try {
			synchronized (this) {
				this.wait(1000);
			}
		} catch(InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		
		/*
		 * (3) Check if event is receieved
		 */
   		Assert.assertEquals(1, eventSink.size());
		Validator v = new Validator().checkModel(eventSink.get(0));
		assertTrue("The created event did not pass the PLAY sanity checks for events.", v.isValid());
	}
	
	@Test
	public void test4store() throws EcConnectionmanagerException {
		EcConnectionManager4store eccm = new EcConnectionManager4store(FOURSTORE_URI, new DistributedEtalis("Detalis"));
		final String cloudId = "http://domain.invalid/testCloud";
		
		Event ev = EventHelpers.builder()
				.stream(cloudId + Stream.STREAM_ID_SUFFIX)
				.addProperty("http://domain.invalid/myProp", "Hello World!")
				.build();
		CompoundEvent event = EventCloudHelpers.toCompoundEvent(ev);
		
		long start;
		start = System.currentTimeMillis();
		eccm.putDataInCloudUsingSparqlUpdate(event, cloudId);
		logger.info("putDataInCloudUsingSparqlUpdate used: " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		eccm.putDataInCloudUsingGraphStoreProtocol(event, cloudId);
		logger.info("putDataInCloudUsingGraphStoreProtocol used: " + (System.currentTimeMillis() - start));
		
		eccm.destroy();
		
		assertEquals(2, rdfSink.size());
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		try {
			notifyReceiverRest.stop();
		} catch (Exception e) {
			logger.error("Exception while stoppping REST server. Nothing we can do now. " + e.getMessage());
		}
		notifyReceiverRest.destroy();
        logger.info("Test server stopped.");
	}

	@Singleton
	public static class TestListenerRest extends Application implements PublishService {

		private final List<Model> eventSink;
		private final Logger logger = LoggerFactory.getLogger(TestListenerRest.class);
		private final AbstractReceiverRest rdfReceiver = new AbstractReceiverRest() {};

		public TestListenerRest() {  // For JAXB
			this.eventSink = null;
		}
		
		public TestListenerRest(List<Model> eventSink) {
			this.eventSink = eventSink;
	        logger.info("Test listener started.");
		}
		
		@Override
		public Response notify(String resource, String message) {
			logger.info("Test listener received event.");
			try {
				eventSink.add(rdfReceiver.parseRdfRest(message));
			} catch (NoRdfEventException e) {
				logger.error("Test listener encountered error.", e);
				Assert.fail("Test listener encountered error: " + e.getMessage());
			}
			return null;
		}
	}
	
	@Path(FOURSTORE_PATH) // overwrite the Path from interface PublishService for this test
	@Singleton
	public static class TestFourstore extends Application {

		private List<String> rdfSink;

		public TestFourstore() {}  // For JAXB

		public TestFourstore(List<String> rdfSink) {
			this.rdfSink = rdfSink;
		}

		@POST
		@Path(EcConnectionManager4store.UPDATE_PATH)
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		public Response update(@FormParam(value = "update") String update) {
			logger.info("update\n: " + update);
			this.rdfSink.add(update);
			return Response.ok().build();
		}
		
		@POST
		@Path(EcConnectionManager4store.DATA_PATH)
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		public Response data(@FormParam("mime-type") String mimeType,
				@FormParam("graph") String graph, @FormParam("data") String data) {
			logger.info(String.format("graph: '%s' mime-type: '%s' data:\n%s", graph, mimeType, data));
			this.rdfSink.add(data);
			return Response.ok().build();
		}
	}
	
	/**
	 * Manual test using external SPARQL endpoint.
	 */
	public static void main(String[] args) throws EcConnectionmanagerException {
		EcConnectionManager4store eccm = new EcConnectionManager4store("http://app.event-processing.org/4store", new DistributedEtalis("Detalis"));
		
		String query =
				" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " SELECT * WHERE {"
				+ "   ?s ?p ?o"
				+ " } LIMIT 10";
		
		SelectResults result = eccm.getDataFromCloud(query, "");
		System.out.println(result.getSize());
		
	}
}

