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

import eu.play_project.dcep.distributedetalis.EventCloudHelpers;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import fr.inria.eventcloud.api.CompoundEvent;


public class ConnectPSandDCEPTest implements Serializable {
	private static final long serialVersionUID = -2703025479714729397L;
	public static QueryDispatchApi queryDispatchApi;
	public static DistributedEtalisTestApi testApi;
	boolean start = false;
	static Component root;
	public static boolean test;

//	@Test
	public void readQueryFromFileTest(){
		System.out.println(getSparqlQuerys("play-epsparql-m12-jeans-example-query.eprq"));
	}
	
	//@Test
	public void instantiatePlayPlatformTest() throws IllegalLifeCycleException,
			NoSuchInterfaceException, ADLException, InterruptedException, QueryDispatchException {

		String queryString;
		
		InstantiatePlayPlatform();

		// Get query.
		queryString = getSparqlQuerys("play-epsparql-m12-jeans-example-query.eprq");
		//queryString = getSparqlQuerys("play-epsparql-contextualized-latitude-01-query.eprq");
		//queryString = getSparqlQuerys("play-epsparql-clic2call.eprq");
		
		//System.out.println("SPARQL query:\n" + queryString);

		
		// Test
		// Compile query
		String paternID = queryDispatchApi.registerQuery("http://test.example.com", queryString);

		
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
			for (int i = 1; i < 1; i++) {
				if (i % 100 == 0) {
					System.out.println("Sent " + i + " Events");
				}
				// subscriber.setState(false);
				testApi.publish(createEvent("http://exmaple.com/eventId/" + i));
			}
		
			//
			Thread.sleep(5000);

		
		// Test if result is OK
		assertTrue("Number of complex events wrong "
				+ subscriber.getComplexEvents().size(), subscriber
				.getComplexEvents().size() == 16);
		assertTrue(
				"Unexpected graph ID "
						+ subscriber.getComplexEvents().get(0).getGraph()
								.toString(),
				subscriber
						.getComplexEvents()
						.get(0)
						.getGraph()
						.toString()
						.equals("http://events.event-processing.org/ids/e2#event"));
		
		
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

		root = (Component) factory.newComponent("EcDcepPsTest", context);
		GCM.getGCMLifeCycleController(root).startFc();

		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root
				.getFcInterface("QueryDispatchApi"));
		testApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root
				.getFcInterface("DistributedEtalisTestApi"));
	}
	
	private String getSparqlQuerys(String queryFile){
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
