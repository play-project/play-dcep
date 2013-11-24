package eu.play_project.dcep.distribution.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

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

import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;
import eu.play_project.dcep.distribution.tests.single_pattern.SingleDistributedEtalisInstancePublisher;
import eu.play_project.play_commons.constants.Namespace;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;

public class ConnectToExistingInstance {
	static PublishApi dcepPublishApi;
	static DcepManagmentApi dcepManagmentApi;
	static ConfigApi configApi;
	static int startTime = 0;
	static Timer timer;
	boolean timeUp = false;
	public static int sendetEvents = 0;

	public static void main(String[] args) throws DcepManagementException, IOException, NamingException, DistributedEtalisException {
		//CentralPAPropertyRepository.PA_NET_INTERFACE.setValue("");
		//CentralPAPropertyRepository.PA_NET_USE_IP_ADDRESS.internalSetValue("141.3.196.15");
	
		// Get connection
		connectToCepEngine("dEtalis", "141.52.218.16");
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
			dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root
					.getFcInterface(DcepManagmentApi.class.getSimpleName()));
			configApi = ((eu.play_project.dcep.distributedetalis.api.ConfigApi) root
					.getFcInterface(ConfigApi.class.getSimpleName()));
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		//Configure dEtalis instance.
		configApi.setConfig(new DetalisConfigLocal("play-epsparql-clic2call-historical-data.trig"));
		
		// Register query
		dcepManagmentApi.registerEventPattern(generateEle(getSparqlQueries("play-bdpl-crisis-01a-radiation.eprq")));
		

		System.out.println("t_1: \t" + System.currentTimeMillis());
	}

	public static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static CompoundEvent createEvent(String eventId, int value,
			String type) {

		List<Quadruple> quads = new ArrayList<Quadruple>();

		Quadruple q1 = new Quadruple(
				NodeFactory.createURI("http://prefix.example.com/" + eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				NodeFactory.createURI("http://prefix.example.com/" + type));

		Quadruple q2 = new Quadruple(
				NodeFactory.createURI("http://prefix.example.com/" + eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://prefix.example.com/value"),
				NodeFactory.createURI(System.currentTimeMillis() + ""));

		Quadruple q3 = new Quadruple(
				NodeFactory.createURI("http://prefix.example.com/" + eventId),
				NodeFactory.createURI("http://prefix.example.com/e1"),
				NodeFactory.createURI("http://prefix.example.com/math/value"),
				NodeFactory.createURI(value + ""));

		quads.add(q1);
		quads.add(q3);
		quads.add(q2);

		return new CompoundEvent(quads);
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
	
	public static BdplQuery generateEle(String queryString) {
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		// Use custom visitor
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";
		//String patternId = "'p1'";
		visitor1.setPatternId(patternId);

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		// Parse query
		Query q;
		try {
			q = QueryFactory.create(queryString, Syntax.syntaxBDPL);
		} catch (com.hp.hpl.jena.query.QueryException e) {
			throw new IllegalArgumentException("Error compiling BDPL to ELE.", e);
		}

		BdplQuery bdplQuery = BdplQuery.builder()
				.ele(etalisPattern)
				.details(new QueryDetails(patternId))
				.bdpl(queryString)
				.constructTemplate(new QueryTemplateGenerator().createQueryTemplate(q))
				.historicalQueries(PlaySerializer.serializeToMultipleSelectQueries(q))
				.build();
		
		return bdplQuery;
	}

}
