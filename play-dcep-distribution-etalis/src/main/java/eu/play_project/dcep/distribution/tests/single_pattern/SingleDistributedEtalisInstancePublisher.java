package eu.play_project.dcep.distribution.tests.single_pattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
import eu.play_project.dcep.api.ConfigApi;
import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.DcepTestApi;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Connect to a DistributedEtalis instance and use the api(s).
 * @author sobermeier
 *
 */
public class SingleDistributedEtalisInstancePublisher {

	private static DcepTestApi testApiI1;
	private static DcepManagmentApi managementApiI1;
	private static ConfigApi configApi = null;

	public SingleDistributedEtalisInstancePublisher(){}
	
	
	public static void main(String[] args) throws IOException, NamingException, DcepManagementException, DistributedEtalisException, QueryDispatchException {

		// Connect to DistributedEtalis instance 1.
		connectToCepEngine("dEtalis", "141.52.218.16");

		// Register queries.
		try {
			//Configure dEtalis instance.
			configApi.setConfigLocal("play-epsparql-clic2call-historical-data.trig");
			
			managementApiI1.registerEventPattern(createCepQuery("p1", getSparqlQueries("benchmarks/srbench/q3.eprq")));
			

		} catch (DcepManagementException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		System.out.println(getSparqlQueries("benchmarks/srbench/q1.eprq"));
		System.out.println("t_1: \t" + System.currentTimeMillis());

		// Publish some events to instance 1.
//		for (org.ontoware.rdf2go.model.Model m : new SrBenchExtendedSimulator()) {
//			//testApiI1.publish(EventCloudHelpers.toCompoundEvent(m));
//			testApiI1.publish(createEvent(Math.random() + ""));
//			delay(20);
//		}
		
		for (int i = 0; i < 20000; i++) {
			System.out.println(i);
			testApiI1.publish(createEvent(i + ""));
			delay(10);
		}
	}
	
	private static void connectToCepEngine(String name, String host) throws IOException, NamingException, DcepManagementException, DistributedEtalisException{

		/* COMPONENT_ALIAS = "Dispatcher" */
		PAComponentRepresentative root = null;

		try {
			root = Fractive.lookup((URIBuilder.buildURI(host, name, "pnp", Integer.parseInt(DcepConstants.getProperties().getProperty("dcep.proactive.pnp.port"))).toString()));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}


		try {
			 testApiI1 = ((eu.play_project.dcep.api.DcepTestApi) root
					.getFcInterface(DcepTestApi.class.getSimpleName()));
			
			managementApiI1 = ((eu.play_project.dcep.api.DcepManagmentApi) root
					.getFcInterface(DcepManagmentApi.class.getSimpleName()));
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
	}


	private static BdplQuery createCepQuery(String queryId, String query)
			throws QueryDispatchException {
		// Parse query
		Query q;
		try {
			q = QueryFactory.create(query, Syntax.syntaxBDPL);
			q.setQueryId(query);
		} catch (com.hp.hpl.jena.query.QueryException e) {
			throw new QueryDispatchException(e.getMessage());
		}
		EleGenerator eleGenerator = new EleGeneratorForConstructQuery();
		
		// Generate CEP-language
		eleGenerator.generateQuery(q);

		// Add queryDetails
		QueryDetails qd = createQueryDetails(queryId, q);
		qd.setRdfDbQueries(eleGenerator.getRdfDbQueries());
		
		BdplQuery bdpl = BdplQuery.builder()
				.details(qd)
				.ele(eleGenerator.getEle())
				.historicalQueries(PlaySerializer.serializeToMultipleSelectQueries(q))
				.constructTemplate(new QueryTemplateGenerator().createQueryTemplate(q))
				.bdpl(query)
				.build();

		return bdpl;
	}
	
	
	private static QueryDetails createQueryDetails(String queryId, Query query) throws QueryDispatchException {
		
		
		QueryDetails qd = new QueryDetails(queryId);

		// Set properties for windows in QueryDetails
		ElementWindowVisitor windowVisitor = new WindowVisitor(qd);
		query.getWindow().accept(windowVisitor);
		
		// Set stream ids in QueryDetails.
		StreamIdCollector streamIdCollector = new StreamIdCollector();
		streamIdCollector.getStreamIds(query, qd);
		
		// Set complex event type.
		qd.setComplexType((new ComplexTypeFinder()).visit(query.getConstructTemplate()));

		return qd;
	}

	public static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static String getSparqlQueries(String queryFile){
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

		List<Quadruple> quads = new ArrayList<Quadruple>();

		 Quadruple q1 = new Quadruple(
				 NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				 NodeFactory.createURI("http://prefix.example.com/e1"),
				 NodeFactory.createURI("http://events.event-processing.org/types/stream"),
				 NodeFactory.createURI("http://streams.event-processing.org/ids/Srbench#stream"));
		 
		 Quadruple q3 = new Quadruple(
				 NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				 NodeFactory.createURI("http://prefix.example.com/e2"),
				 NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#WindSpeedObservation"));
		 
		 Quadruple q4 = new Quadruple(
				 NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				 NodeFactory.createURI("http://prefix.example.com/e2"),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#observedProperty"),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#_WindSpeed"));
		 
		 Quadruple q5 = new Quadruple(
				 NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				 NodeFactory.createURI("http://prefix.example.com/e2"),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"));
		 
		 Quadruple q6 = new Quadruple(
				 NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
				 NodeFactory.createURI("10.0"));
		 
		 Quadruple q7 = new Quadruple(
				 NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				 NodeFactory.createURI("http://prefix.example.com/e1"),
				 NodeFactory.createURI("http://events.event-processing.org/types/endTime"),
				 NodeFactory.createURI(new SimpleDateFormat(eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601).format(new Date())));
		 
		 Quadruple q8 = new Quadruple(
				 NodeFactory.createURI("http://events.event-processing.org/eventId/" + eventId),
				 NodeFactory.createURI("http://prefix.example.com/e2"),
				 NodeFactory.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#procedure"),
				 NodeFactory.createURI("http://sensor.example.com/S1"));


		


//		Quadruple q3 = new Quadruple(
//				NodeFactory.createURI("http://prefix.example.com/" + eventId),
//				NodeFactory.createURI("http://prefix.example.com/e1"),
//				NodeFactory.createURI("http://prefix.example.com/math/value"),
//				NodeFactory.createURI(value + ""));

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

