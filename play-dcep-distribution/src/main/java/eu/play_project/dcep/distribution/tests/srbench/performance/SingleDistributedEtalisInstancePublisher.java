package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;
import org.ow2.play.srbench.SrBenchExtendedSimulator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
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
public class SingleDistributedEtalisInstancePublisher {

	private static DistributedEtalisTestApi testApiI1;
	private static DcepManagmentApi managementApiI1;
	private static DistributedEtalisTestApi testApiI2;
	private static DcepManagmentApi managementApiI2;

	public static void main(String[] args) throws RemoteException,
			NotBoundException, Exception {
		
		MeasurementUnit meausrementUnit = new MeasurementUnit();

		// Connect to DistributedEtalis instance 1.
		PAComponentRepresentative root1 = Fractive.lookup(URIBuilder.buildURI(args[0], args[1], "rmi", 1099).toString());
		testApiI1 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root1.getFcInterface("DistributedEtalisTestApi"));
		managementApiI1 = ((eu.play_project.dcep.api.DcepManagmentApi) root1.getFcInterface("DcepManagmentApi"));
		
		// Connect to DistributedEtalis instance 2.
		PAComponentRepresentative root2 = Fractive.lookup(URIBuilder.buildURI(args[2], args[3], "rmi", 1099).toString());
		testApiI2 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root2.getFcInterface("DistributedEtalisTestApi"));
		managementApiI2 = ((eu.play_project.dcep.api.DcepManagmentApi) root2.getFcInterface("DcepManagmentApi"));

		//Register queries.
		managementApiI1.registerEventPattern(generateEle(getSparqlQueries("benchmarks/srbench/q3.eprq")));
		managementApiI2.registerEventPattern(generateEle(getSparqlQueries("benchmarks/srbench/q3.eprq")));
		
		meausrementUnit.calcRateForNEvents(100);

		// Publish some events
		for (org.ontoware.rdf2go.model.Model m : new SrBenchExtendedSimulator()) {
			testApiI1.publish(EventCloudHelpers.toCompoundEvent(m));
			meausrementUnit.nexEvent();
			testApiI2.publish(EventCloudHelpers.toCompoundEvent(m));
			meausrementUnit.nexEvent();
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
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	
	}
}
