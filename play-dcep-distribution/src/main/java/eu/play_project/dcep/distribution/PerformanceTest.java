package eu.play_project.dcep.distribution;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.etsi.uri.gcm.util.GCM;
import org.event_processing.events.types.FacebookStatusFeedEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;

public class PerformanceTest {

	private static final String propertiesFile = "proactive.java.policy";
	private static Set<CompoundEvent> events = new HashSet<CompoundEvent>();
	private static QueryDispatchApi queryDispatchApi;
	private static PublishApi dcepPublishApi;
	private static Component root;
	private static Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

	public static void main(String[] args) throws ADLException, IllegalLifeCycleException,
			NoSuchInterfaceException, InterruptedException, QueryDispatchException {

		String queryString;

		instantiatePlayPlatform();

		// Get query.
		queryString = getSparqlQuery("play-epsparql-m12-jeans-example-query.eprq");
		// queryString =
		// getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
		// queryString = getSparqlQuery("play-epsparql-clic2call.eprq");

		// System.out.println("SPARQL query:\n" + queryString);

		// Test
		// Compile query
		String patternId = queryDispatchApi.registerQuery("http://test.example.com", queryString);

		// Subscribe to get complex events.
		SubscriberPerformanceTest subscriber = null;
		try {
			subscriber = PAActiveObject.newActive(SubscriberPerformanceTest.class, new Object[] {});
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}

		for (int i = 1; i < 100000; i++) {
			// System.out.println("i = " + i);
			// if(i%1000==0){
			// System.out.println("Send " + i + " Events");
			// }

			dcepPublishApi.publish(createEvent(i + ""));
			Thread.sleep(70);

		}

		// Push events.
		// dcepPublishApi.publish(new CompoundEvent (quadruple2)); // Fist event
		// boolean wahr = true;
		// while(true&& wahr){
		// Thread.sleep(2200);
		// //System.out.println(subscriber.finished());
		// for (int i = 1; i < 2000; i++) {
		// //System.out.println("i = " + i);
		// if(i%1000==0){
		// System.out.println("Send " + i + " Events");
		// }
		//
		// dcepPublishApi.publish(createEvent("2620:0:1C11:e::" + i));
		// }
		// }

		// Wait
		Scanner in = new Scanner(System.in);

		// Test if result is OK
		// assertTrue("Number of complex events wrong "
		// + subscriber.getComplexEvents().size(), subscriber
		// .getComplexEvents().size() == 16);
		// assertTrue(
		// "Unexpected graph ID "
		// + subscriber.getComplexEvents().get(0).getGraph()
		// .toString(),
		// subscriber
		// .getComplexEvents()
		// .get(0)
		// .getGraph()
		// .toString()
		// .equals("http://events.event-processing.org/ids/e2#event"));

		// Stop and terminate GCM Components
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			// Terminate all subcomponents.
			for (Component subcomponent : GCM.getContentController(root).getFcSubComponents()) {
				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
			}

		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
	}

	private static String getSparqlQuery(String queryFile) {
		try {
			InputStream is = PerformanceTest.class.getClassLoader().getResourceAsStream(queryFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;

			while (null != (line = br.readLine())) {
				sb.append(line);
				sb.append("\n");
			}
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			logger.error("Error reading query from a file.", e);
		}
		return null;

	}

	private static void instantiatePlayPlatform()
			throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException {

		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
				.setValue("proactive.java.policy");

		CentralPAPropertyRepository.GCM_PROVIDER
				.setValue("org.objectweb.proactive.core.component.Fractive");

		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		root = (Component) factory.newComponent("EcDcepPsTest", context);
		GCM.getGCMLifeCycleController(root).startFc();

		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root
				.getFcInterface(QueryDispatchApi.class.getSimpleName()));
		dcepPublishApi = ((fr.inria.eventcloud.api.PublishApi) root
				.getFcInterface(PublishApi.class.getSimpleName()));
	}

	public static CompoundEvent createEvent(String eventId) {

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

		// Push events.
		return EventCloudHelpers.toCompoundEvent(event);
	}
}
