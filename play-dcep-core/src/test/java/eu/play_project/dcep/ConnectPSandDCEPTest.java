package eu.play_project.dcep;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.event_processing.events.types.FacebookStatusFeedEvent;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import fr.inria.eventcloud.api.CompoundEvent;


public class ConnectPSandDCEPTest implements Serializable {
	private static final long serialVersionUID = 100L;
	public static QueryDispatchApi queryDispatchApi;
	public static DistributedEtalisTestApi testApi;
	boolean start = false;
	static Component root;
	public static boolean test;

	@Ignore
	@Test
	public void readQueryFromFileTest(){
		System.out.println(getSparqlQueries("play-epsparql-m12-jeans-example-query.eprq"));
	}
	
	@Ignore
	@Test
	public void instantiatePlayPlatformTest() throws IllegalLifeCycleException,
			NoSuchInterfaceException, ADLException, InterruptedException, QueryDispatchException {

		String queryString;
		
		InstantiatePlayPlatform();

		// Get query.
		queryString = getSparqlQueries("play-epsparql-m12-jeans-example-query.eprq");
		//queryString = getSparqlQueries("play-epsparql-contextualized-latitude-01-query.eprq");
		//queryString = getSparqlQueries("play-epsparql-clic2call.eprq");
		
		//System.out.println("SPARQL query:\n" + queryString);


		// Compile query
		String patternId = queryDispatchApi.registerQuery("http://test.example.com", queryString);

		
		//Subscribe to get complex events.
		SubscriberPerformanceTest subscriber = null;
		try {
			subscriber = PAActiveObject.newActive(SubscriberPerformanceTest.class, new Object[] {});
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}

		testApi.attach(subscriber);


	
		// Push events.
			for (int i = 1; i < 19; i++) {
				if (i % 100 == 0) {
					System.out.println("Sent " + i + " Events");
				}
				// subscriber.setState(false);
				testApi.publish(createEvent("http://exmaple.com/eventId/" + i));
				Thread.sleep(1);
			}
		
			//
			Thread.sleep(2000);

		
		// Test if result is OK
		assertTrue("Number of complex events wrong "
				+ subscriber.getComplexEvents().size(), subscriber
				.getComplexEvents().size() == 16);
		
		
		// Stop and terminate GCM Components
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			// Terminate all subcomponents.
			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
			 }

			
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
}

	/**
	 * Test tumbling window functionality.
	 * The registered pattern generates a complex event if three simple events appear in one second (tumbling window).
	 * In the beginning of the test four events (e1,e2,e3,e4) are sent and two seconds later two additional events (e5,e6).  After a break three events (e7,e8,e9) are sent.
	 * This test will produce three complex events. The first four events generate two complex events (c1,c2). After the break of two
	 * seconds a new window (w2) opens and three events are needed to fulfill the pattern. But only two events (e5, e6) are sent - no complex event.
	 * In window w3 three events are sent.  For this reason one complex event (c3) is produced.
	 * 
	 * w1                        w2                        w3                       w4
	 * |-e1----e2----e3----e4----|-----------e5----e6------|-e7----e8----e9----------|
	 *               |-> c1 |-> c2                                       |-> c3
	 */
	@Ignore
	@Test
	public void tumblingWindowTest() throws IllegalLifeCycleException,
			NoSuchInterfaceException, ADLException, InterruptedException, QueryDispatchException {

		String queryString;
		
		InstantiatePlayPlatform();

		// Get query.
		queryString = getSparqlQueries("patterns/play-epsparql-m12-jeans-example-query-tumbling-window.eprq");

		// Compile query
		String patternId = queryDispatchApi.registerQuery("http://test.example.com", queryString);

		
		//Subscribe to get complex events.
		SubscriberPerformanceTest subscriber = null;
		try {
			subscriber = PAActiveObject.newActive(SubscriberPerformanceTest.class, new Object[] {});
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}

		testApi.attach(subscriber);

		// Push  event e1,e2,e3.
		testApi.publish(createEvent("http://exmaple.com/eventId/e1"));
		Thread.sleep(1);
		testApi.publish(createEvent("http://exmaple.com/eventId/e2"));
		Thread.sleep(1);
		testApi.publish(createEvent("http://exmaple.com/eventId/e3"));
		Thread.sleep(1);
		testApi.publish(createEvent("http://exmaple.com/eventId/e4"));
		Thread.sleep(1);
		
		// Wait
		Thread.sleep(2000);
		
		// Check if c1 and c2 appeared.
		assertTrue("Number of complex events wrong.  "+ subscriber.getComplexEvents().size() + " complex events produced but 2 complex event were expected.", subscriber.getComplexEvents().size() == 2);
		
		testApi.publish(createEvent("http://exmaple.com/eventId/e5"));
		Thread.sleep(1);
		testApi.publish(createEvent("http://exmaple.com/eventId/e6"));
		Thread.sleep(1);
		
		// No new complex event was produced.
		assertTrue("Number of complex events wrong "+ subscriber.getComplexEvents().size(), subscriber.getComplexEvents().size() == 2);
		
		// Wait
		Thread.sleep(2000);
		
		// Send e7-e9
		testApi.publish(createEvent("http://exmaple.com/eventId/e7"));
		Thread.sleep(1);
		testApi.publish(createEvent("http://exmaple.com/eventId/e8"));
		Thread.sleep(1);
		testApi.publish(createEvent("http://exmaple.com/eventId/e9"));
		Thread.sleep(1);
		
		// Wait to process events.
		Thread.sleep(2000);
		
		// Check if c3 was produced
		assertTrue("Number of complex events wrong "+ subscriber.getComplexEvents().size(), subscriber.getComplexEvents().size() == 3);
		
		
		// Stop and terminate GCM Components
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			// Terminate all subcomponents.
			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
			 }

			
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
}

	public void sendEvents(){
		start = true;
		System.out.println("Start Producer");
		System.out.println("Send 2000 Events \n\n\n\n\n\n\n");
		
	}
	
	public static CompoundEvent createEvent(String eventId){
	
		FacebookStatusFeedEvent event = new FacebookStatusFeedEvent(
				// set the RDF context part
				EventHelpers.createEmptyModel(eventId),
				// set the RDF subject
				eventId + EVENT_ID_SUFFIX,
				// automatically write the rdf:type statement
				true);
		
		// Run some setters of the event
		event.setFacebookName("Roland Stühmer");
		event.setFacebookId("100000058455726");
		event.setFacebookLink(new URIImpl("http://graph.facebook.com/roland.stuehmer#"));
		event.setStatus("I bought some JEANS this morning");
		event.setFacebookLocation("Karlsruhe, Germany");
		// Create a Calendar for the current date and time
		event.setEndTime(Calendar.getInstance());
		event.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));

		//Push events.
		return EventCloudHelpers.toCompoundEvent(event);
	}

	public static void InstantiatePlayPlatform()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
				.setValue("proactive.java.policy");

		CentralPAPropertyRepository.GCM_PROVIDER
				.setValue("org.objectweb.proactive.core.component.Fractive");

		
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		root = (Component) factory.newComponent("PsDcepComponent", context);
		GCM.getGCMLifeCycleController(root).startFc();

		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root
				.getFcInterface(QueryDispatchApi.class.getSimpleName()));
		testApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root
				.getFcInterface(DistributedEtalisTestApi.class.getSimpleName()));
	}
	
	private String getSparqlQueries(String queryFile){
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
			BufferedReader br =new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;
			
			while (null != (line = br.readLine())) {
					sb.append(line);
					sb.append("\n");
			}
			//System.out.println(sb.toString());
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	
	}
}
