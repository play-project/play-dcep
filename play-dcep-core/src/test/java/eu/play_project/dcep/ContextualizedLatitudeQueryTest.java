//package eu.play_project.dcep;
//
//import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Scanner;
//
//import org.etsi.uri.gcm.util.GCM;
//import org.event_processing.events.types.TaxiUCGeoLocation;
//import org.junit.Test;
//import org.objectweb.fractal.adl.ADLException;
//import org.objectweb.fractal.adl.Factory;
//import org.objectweb.fractal.api.Component;
//import org.objectweb.fractal.api.NoSuchInterfaceException;
//import org.objectweb.fractal.api.control.IllegalLifeCycleException;
//import org.objectweb.proactive.ActiveObjectCreationException;
//import org.objectweb.proactive.api.PAActiveObject;
//import org.objectweb.proactive.core.component.adl.FactoryFactory;
//import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
//import org.objectweb.proactive.core.node.NodeException;
//import org.ontoware.rdf2go.model.node.impl.URIImpl;
//
//import eu.play_project.dcep.api.DcepSubscribeApi;
//import eu.play_project.dcep.distributedetalis.EventCloudHelpers;
//import eu.play_project.play_commons.constants.Stream;
//import eu.play_project.play_commons.eventtypes.EventHelpers;
//import eu.play_project.play_platformservices.api.QueryDispatchApi;
//import fr.inria.eventcloud.api.CompoundEvent;
//import fr.inria.eventcloud.api.PublishApi;
//
//public class ContextualizedLatitudeQueryTest {
//
//	public static QueryDispatchApi queryDispatchApi;
//	public static PublishApi dcepPublishApi;
//	public static DcepSubscribeApi dcepSubscribeApi;
//	static Component root;
//
//	@Test
//	public void instantiatePlayPlatformTest() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, InterruptedException {
//
//		String queryString;
//
//		InstantiatePlayPlatform();
//
//		// Get query.
//		queryString = getSparqlQueries("play-epsparql-contextualized-latitude-01-query.eprq");
//
//
//		System.out.println("SPARQL query:\n" + queryString);
//
//		// Test
//		// Compile query
//		String paternID = queryDispatchApi.registerQuery("http://test.example.com", queryString, "http://streams.event-processing.org/ids/default");
//
//		// Subscribe to get complex events.
//		PublishApiSubscriber subscriber = null;
//		try {
//			subscriber = PAActiveObject.newActive(PublishApiSubscriber.class, new Object[] {});
//		} catch (ActiveObjectCreationException e) {
//			e.printStackTrace();
//		} catch (NodeException e) {
//			e.printStackTrace();
//		}
//
//		boolean success = dcepSubscribeApi.attach(subscriber);
//
//		for (int i = 1; i < 8; i++) {
//				System.out.println("Send " + i + " Events");
//
//			dcepPublishApi.publish(createEvent("2620:0:1C11:e::" + i));
//
//			Thread.sleep(70);
//		}
//
//
//		Thread.sleep(100000);
//
//		// Wait
//		Scanner in = new Scanner(System.in);
//		in.nextLine();
//
//		// Test if result is OK
//		// assertTrue("Number of complex events wrong "
//		// + subscriber.getComplexEvents().size(), subscriber
//		// .getComplexEvents().size() == 16);
//		// assertTrue(
//		// "Unexpected graph ID "
//		// + subscriber.getComplexEvents().get(0).getGraph()
//		// .toString(),
//		// subscriber
//		// .getComplexEvents()
//		// .get(0)
//		// .getGraph()
//		// .toString()
//		// .equals("http://events.event-processing.org/ids/e2#event"));
//
//		// Stop and terminate GCM Components
//		try {
//			GCM.getGCMLifeCycleController(root).stopFc();
//			// Terminate all subcomponents.
//			for (Component subcomponent : GCM.getContentController(root).getFcSubComponents()) {
//				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
//			}
//
//		} catch (IllegalLifeCycleException e) {
//			e.printStackTrace();
//		} catch (NoSuchInterfaceException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static CompoundEvent createEvent(String eventId) {
//
//		TaxiUCGeoLocation event = new TaxiUCGeoLocation(
//				// set the RDF context part
//				EventHelpers.createEmptyModel(eventId),
//				// set the RDF subject
//				eventId + EVENT_ID_SUFFIX,
//				// automatically write the rdf:type statement
//				true);
//
//		// Run some setters of the event
//		EventHelpers.addLocationToEvent(event, 7, 6);
//		event.setUctelcoTwId("rolandstuehmer");
//		event.setUctelcoPhoneNumber("Alice");
//		event.setStream(new URIImpl(Stream.TaxiUCGeoLocation.getUri()));
//
//		// Create a Calendar for the current date and time
//		event.setEndTime(Calendar.getInstance());
//
//		// Push events.
//		return EventCloudHelpers.toCompoundEvent(event);
//	}
//
//	public static void InstantiatePlayPlatform() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException {
//
//		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue("proactive.java.policy");
//
//		CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
//
//		Factory factory = FactoryFactory.getFactory();
//		HashMap<String, Object> context = new HashMap<String, Object>();
//
//		root = (Component) factory.newComponent("EcDcepPsTest", context);
//		GCM.getGCMLifeCycleController(root).startFc();
//
//		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));
//		dcepPublishApi = ((fr.inria.eventcloud.api.PublishApi) root.getFcInterface("PublishApi"));
//		dcepSubscribeApi = ((eu.play_project.dcep.api.DcepSubscribeApi) root.getFcInterface("DcepSubscribeApi"));
//
//	}
//
//	private String getSparqlQueries(String queryFile) {
//		try {
//			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));
//			StringBuffer sb = new StringBuffer();
//			String line;
//
//			while (null != (line = br.readLine())) {
//				sb.append(line);
//				sb.append("\n");
//			}
//			// System.out.println(sb.toString());
//			br.close();
//			is.close();
//
//			return sb.toString();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//
//	}
//}
