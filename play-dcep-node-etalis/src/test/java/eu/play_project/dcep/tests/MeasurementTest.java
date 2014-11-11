package eu.play_project.dcep.tests;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.NodeFactory;

import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.api.measurement.MeasurementConfig;
import eu.play_project.dcep.api.measurement.NodeMeasurementResult;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class MeasurementTest {

	public static QueryDispatchApi queryDispatchApi;
	public static DcepNodeApi<CompoundEvent> testApi;
	public static DcepMonitoringApi monitoringApi;
	boolean start = false;
	static Component root;
	public static boolean test;
	private final org.slf4j.Logger logger = LoggerFactory.getLogger(MeasurementTest.class);

	@Test
	public void basicMeasurementTest() throws IllegalLifeCycleException,
			NoSuchInterfaceException, ADLException, InterruptedException,
			QueryDispatchException {

		String queryString;

		instantiatePlayPlatform();

		// Get query.
		queryString = getSparqlQueries("patterns/measurement.eprq");

		// Compile query
		queryDispatchApi.registerQuery("measurement", queryString);

		for (int i = 0; i < 1; i++) {

			monitoringApi.measurePerformance(new MeasurementConfig(1000, null));

			// Wait and pull data
			Thread.sleep(2000);

			NodeMeasurementResult dEtalis1Data = monitoringApi.getMeasuredData("measurement");
			
			System.out.println(dEtalis1Data.getMeasuredValues());
			System.out.println(dEtalis1Data.getNumberOfComponentInputEvetns());
			System.out.println(dEtalis1Data.getNumberOfOutputEvents());
			//printUtilisation(dEtalis1Data);
		}

			// Stop and terminate GCM Components
			try {
				GCM.getGCMLifeCycleController(root).stopFc();
				// Terminate all subcomponents.
				for (Component subcomponent : GCM.getContentController(root)
						.getFcSubComponents()) {
					logger.info("Terminating component: "
							+ subcomponent.getFcType());
					GCM.getGCMLifeCycleController(subcomponent)
							.terminateGCMComponent();
				}

			} catch (IllegalLifeCycleException e) {
				e.printStackTrace();
			} catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
	}


	public static void instantiatePlayPlatform()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		root = ProActiveHelpers.newComponent("PsDcepComponent");
		GCM.getGCMLifeCycleController(root).startFc();

		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root
				.getFcInterface(QueryDispatchApi.class.getSimpleName()));
		testApi = (DcepNodeApi<CompoundEvent>) root
				.getFcInterface(DcepNodeApi.class.getSimpleName());
		
		monitoringApi = ((eu.play_project.dcep.api.DcepMonitoringApi) root
				.getFcInterface(DcepMonitoringApi.class.getSimpleName()));

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static CompoundEvent createWeatherEvent(String eventId, double value) {

		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();

		Quadruple q1 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://events.event-processing.org/types/stream"),
				NodeFactory.createURI("http://streams.event-processing.org/ids/Srbench#stream"));

		Quadruple q3 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#WindSpeedObservation"));

		Quadruple q4 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#observedProperty"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#_WindSpeed"));

		Quadruple q5 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"));

		Quadruple q6 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
				NodeFactory.createURI(value + ""));

		// Quadruple q7 = new Quadruple(
		// NodeFactory.createURI("http://events.event-processing.org/eventId/"
		// + eventId),
		// NodeFactory.createURI("http://prefix.example.com/e1"),
		// NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
		// NodeFactory.createURI(new SimpleDateFormat(
		// eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601)
		// .format(new Date())));
		Quadruple q7 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
				NodeFactory.createURI(System.currentTimeMillis() + ""));

		Quadruple q8 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e2"),
				NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#procedure"),
				NodeFactory.createURI("http://sensor.example.com/S1"));

		quads.add(q1);
		// quads.add(q2);
		quads.add(q3);
		quads.add(q4);
		quads.add(q5);
		quads.add(q6);
		quads.add(q7);
		quads.add(q8);

		return new CompoundEvent(quads);
	}

	public static CompoundEvent createFacebookTopicEvent(String eventId) {

		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();

		Quadruple q1 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://events.event-processing.org/types/stream"),
				NodeFactory.createURI("http://streams.event-processing.org/ids/FacebookStatusFeed#stream"));

		Quadruple q3 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				NodeFactory.createURI("http://events.event-processing.org/types/FacebookStatusFeedEvent"));

		Quadruple q4 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://events.event-processing.org/types/status"),
				NodeFactory.createURI("Tea"));

		Quadruple q5 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://events.event-processing.org/types/status"),
				NodeFactory.createURI("Coffee"));

		Quadruple q6 = new Quadruple(
				NodeFactory.createURI("http://events.event-processing.org/eventId/"
						+ eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://graph.facebook.com/schema/user#name"),
				NodeFactory.createURI("Max"));

		quads.add(q1);
		quads.add(q3);
		quads.add(q4);
		quads.add(q5);
		quads.add(q6);

		return new CompoundEvent(quads);
	}

	private String getSparqlQueries(String queryFile) {
		try {
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream(queryFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;

			while (null != (line = br.readLine())) {
				sb.append(line);
				sb.append("\n");
			}
			// System.out.println(sb.toString());
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
}
