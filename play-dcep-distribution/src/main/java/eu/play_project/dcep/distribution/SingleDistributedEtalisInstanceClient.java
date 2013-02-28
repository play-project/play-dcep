package eu.play_project.dcep.distribution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.EleGeneratorForConstructQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Connect to a DistributedEtalis instance and use the api(s).
 * @author Stefan Obermeier
 *
 */
public class SingleDistributedEtalisInstanceClient {

	private static DistributedEtalisTestApi testApi;
	private static DcepManagmentApi managementApi;

	public static void main(String[] args) throws RemoteException,
			NotBoundException, Exception {

		// Connect to DistributedEtalis instance.
		PAComponentRepresentative root;
		// root = Fractive.lookup(URIBuilder.buildURI("2001:6f8:100d:b::1", "dEtalis2", "rmi", 1099).toString());
		// root = Fractive.lookup(URIBuilder.buildURI("172.20.47.169", "dEtalis2", "rmi", 1099).toString());
		root = Fractive.lookup(URIBuilder.buildURI("fe80:0:0:0:492a:79ab:db40:f8b1", "dEtalis1", "rmi", 1099).toString());

		testApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root.getFcInterface("DistributedEtalisTestApi"));
		managementApi = ((eu.play_project.dcep.api.DcepManagmentApi) root.getFcInterface("DcepManagmentApi"));

		//Register query.
		//managementApi.registerEventPattern(generateEle(getSparqlQuerys("3timesA.eprq")));

		// Publish some events.
		for (int i = 0; i < 1000000; i++) {
			testApi.publish(createEvent("timeS" + i, (i % 20), "A"));
			delay(2);
		}
	}
	
	public static CompoundEvent createEvent(String eventId, int value, String type) {

		List quads = new ArrayList();

		 Quadruple q1 = new Quadruple(
				 Node.createURI("http://prefix.example.com/" + eventId),
				 Node.createURI("http://prefix.example.com/e1"),
				 Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				 Node.createURI("http://prefix.example.com/" + type));


		Quadruple q2 = new Quadruple(
				Node.createURI("http://prefix.example.com/" + eventId), 
				Node.createURI("http://prefix.example.com/e1"),
				Node.createURI("http://prefix.example.com/value"), 
				Node.createURI(System.currentTimeMillis() + ""));


//		Quadruple q3 = new Quadruple(
//				Node.createURI("http://prefix.example.com/" + eventId), 
//				Node.createURI("http://prefix.example.com/e1"),
//				Node.createURI("http://prefix.example.com/math/value"), 
//				Node.createURI(value + ""));

		quads.add(q1);
	//	quads.add(q3);
		quads.add(q2);

		return new CompoundEvent(quads);
	}

	private static EpSparqlQuery generateEle(String queryString) {
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
		// Use custom visitor
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		//String patternId = "'http://patternID.example.com/" + Math.random() * 1000000 + "'";
		String patternId = "'p1'";
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

	public static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private static String getSparqlQuerys(String queryFile){
		try {
			InputStream is = SingleDistributedEtalisInstanceClient.class.getClassLoader().getResourceAsStream(queryFile);
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
}
