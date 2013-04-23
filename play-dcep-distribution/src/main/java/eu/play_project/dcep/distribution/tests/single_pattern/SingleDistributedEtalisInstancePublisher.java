package eu.play_project.dcep.distribution.tests.single_pattern;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.event_processing.events.types.UcTelcoCall;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ow2.play.srbench.SrBenchExtendedSimulator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.EleGeneratorForConstructQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Connect to a DistributedEtalis instance and use the api(s).
 * @author sobermeier
 *
 */
public class SingleDistributedEtalisInstancePublisher {

	private static DistributedEtalisTestApi testApiI1;
	private static DcepManagmentApi managementApiI1;


	public static void main(String[] args) throws RemoteException,
			NotBoundException, Exception {

		// Connect to DistributedEtalis instance 1.
		PAComponentRepresentative root1 = Fractive.lookup(URIBuilder.buildURI(args[0], args[1], "rmi", 1099).toString());
		testApiI1 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root1.getFcInterface("DistributedEtalisTestApi"));
		managementApiI1 = ((eu.play_project.dcep.api.DcepManagmentApi) root1.getFcInterface("DcepManagmentApi"));

		// Register queries.
		managementApiI1.registerEventPattern(generateEle(getSparqlQueries("benchmarks/srbench/q3.eprq")));
		//managementApiI1.registerEventPattern(generateEle(getSparqlQueries("play-epsparql-clic2call.eprq")));
		System.out.println(getSparqlQueries("benchmarks/srbench/q3.eprq"));

		// Publish some events to instance 1.
		for (org.ontoware.rdf2go.model.Model m : new SrBenchExtendedSimulator()) {
			//testApiI1.publish(EventCloudHelpers.toCompoundEvent(m));
			testApiI1.publish(createEvent(Math.random() + ""));
			delay(20);
		}
	}


	private static EpSparqlQuery generateEle(String queryString) {
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
		// Use custom visitor
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		String patternId = "http://patternID.example.com/" + Math.random() * 1000000;
		//String patternId = "'p1'";
		visitor1.setPatternId(patternId);

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		EpSparqlQuery epSparqlQuery = new EpSparqlQuery();
		epSparqlQuery.setEleQuery(etalisPattern);

		QueryDetails details = new QueryDetails();
		details.setQueryId(patternId);
		epSparqlQuery.setQueryDetails(details);
		return epSparqlQuery;
	}
	
	public static CompoundEvent createTaxiUCCallEvent(String eventId){
		
		UcTelcoCall event = new UcTelcoCall(
				// set the RDF context part
				EventHelpers.createEmptyModel(eventId),
				// set the RDF subject
				eventId + EVENT_ID_SUFFIX,
				// automatically write the rdf:type statement
				true);

		// Run some setters of the event
		event.setUcTelcoCalleePhoneNumber("49123456789");
		event.setUcTelcoCallerPhoneNumber("49123498765");
		event.setUcTelcoDirection("incoming");
		
		double longitude = 123;
		double latitude = 345;
		EventHelpers.setLocationToEvent(event, longitude, latitude);
		
		// Create a Calendar for the current date and time
		event.setEndTime(Calendar.getInstance());
		event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));

		//Push events.
		return EventCloudHelpers.toCompoundEvent(event);
	}

	public static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private static String getSparqlQueries(String queryFile){
		try {
			InputStream is = SingleDistributedEtalisInstancePublisher.class.getClassLoader().getResourceAsStream(queryFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
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
	public static CompoundEvent createEvent(String eventId) {

		List quads = new ArrayList<Quadruple>();

		 Quadruple q1 = new Quadruple(
				 Node.createURI("http://events.event-processing.org/eventId/" + eventId),
				 Node.createURI("http://prefix.example.com/e1"),
				 Node.createURI("http://events.event-processing.org/types/stream"),
				 Node.createURI("http://streams.event-processing.org/ids/Srbench#stream"));
		 
		 Quadruple q3 = new Quadruple(
				 Node.createURI("http://events.event-processing.org/eventId/" + eventId),
				 Node.createURI("http://prefix.example.com/e2"),
				 Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#WindSpeedObservation"));
		 
		 Quadruple q4 = new Quadruple(
				 Node.createURI("http://events.event-processing.org/eventId/" + eventId),
				 Node.createURI("http://prefix.example.com/e2"),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#observedProperty"),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#_WindSpeed"));
		 
		 Quadruple q5 = new Quadruple(
				 Node.createURI("http://events.event-processing.org/eventId/" + eventId),
				 Node.createURI("http://prefix.example.com/e2"),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"));
		 
		 Quadruple q6 = new Quadruple(
				 Node.createURI("http://events.event-processing.org/eventId/" + eventId),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
				 Node.createURI("10.0"));
		 
		 Quadruple q7 = new Quadruple(
				 Node.createURI("http://events.event-processing.org/eventId/" + eventId),
				 Node.createURI("http://prefix.example.com/e1"),
				 Node.createURI("http://events.event-processing.org/types/endTime"),
				 Node.createURI(new SimpleDateFormat(eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601).format(new Date())));
		 
		 Quadruple q8 = new Quadruple(
				 Node.createURI("http://events.event-processing.org/eventId/" + eventId),
				 Node.createURI("http://prefix.example.com/e2"),
				 Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#procedure"),
				 Node.createURI("http://sensor.example.com/S1"));


		


//		Quadruple q3 = new Quadruple(
//				Node.createURI("http://prefix.example.com/" + eventId), 
//				Node.createURI("http://prefix.example.com/e1"),
//				Node.createURI("http://prefix.example.com/math/value"), 
//				Node.createURI(value + ""));

		quads.add(q1);
	//	quads.add(q2);
		quads.add(q3);
		quads.add(q4);
		quads.add(q5);
		quads.add(q6);
		quads.add(q7);
		quads.add(q8);

		return new CompoundEvent(quads);
	}
}

