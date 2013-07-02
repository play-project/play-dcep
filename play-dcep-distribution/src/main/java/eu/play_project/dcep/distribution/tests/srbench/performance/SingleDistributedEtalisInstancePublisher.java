package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;

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
	private static DistributedEtalisTestApi testApiI3;
	private static DcepManagmentApi managementApiI3;

	public static void main(String[] args) throws RemoteException,
			NotBoundException, Exception {
		

		// Connect to DistributedEtalis instance 1.
		PAComponentRepresentative root1 = Fractive.lookup(URIBuilder.buildURI(args[0], args[1], "rmi", 1099).toString());
		testApiI1 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root1.getFcInterface("DistributedEtalisTestApi"));
		managementApiI1 = ((eu.play_project.dcep.api.DcepManagmentApi) root1.getFcInterface("DcepManagmentApi"));
		
//		// Connect to DistributedEtalis instance 2.
		PAComponentRepresentative root2 = Fractive.lookup(URIBuilder.buildURI(args[2], args[3], "rmi", 1099).toString());
		testApiI2 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root2.getFcInterface("DistributedEtalisTestApi"));
		managementApiI2 = ((eu.play_project.dcep.api.DcepManagmentApi) root2.getFcInterface("DcepManagmentApi"));
		
//		// Connect to DistributedEtalis instance 3.
//		PAComponentRepresentative root3 = Fractive.lookup(URIBuilder.buildURI(args[4], args[5], "rmi", 1099).toString());
//		testApiI3 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root3.getFcInterface("DistributedEtalisTestApi"));
//		managementApiI3 = ((eu.play_project.dcep.api.DcepManagmentApi) root3.getFcInterface("DcepManagmentApi"));

		BdplQuery q = generateEle(getSparqlQueries("benchmarks/srbench/q3.eprq"));
		// Register queries.  mw
		managementApiI1.registerEventPattern(q);
		managementApiI2.registerEventPattern(q);
//		managementApiI3.registerEventPattern(q);
		
		
		// Start publishing events.
		new EventProducerThread(1000000, 40, testApiI1);
		new EventProducerThread(1000000, 40, testApiI2);
		//new EventProducerThread(1000, 1000, testApiI1);
		//new EventProducerThread(1000, 200, testApiI1);
		//new EventProducerThread(1000, 8, testApiI1);
	
		
	}

	private static BdplQuery generateEle(String queryString) {
		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		// Use custom visitor
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		String patternId = "http://patternID.example.com/" + Math.random() * 1000000;
		//String patternId = "'p1'";
		visitor1.setPatternId(patternId);

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();
		
		QueryDetails details = new QueryDetails();
	
		BdplQuery bdplQuery = new BdplQuery();
		bdplQuery.setEleQuery(etalisPattern);

		details.setQueryId(patternId);
		// Set properties for windows in QueryDetails
		ElementWindowVisitor windowVisitor = new WindowVisitor(details);
		query.getWindow().accept(windowVisitor);
		bdplQuery.setQueryDetails(details);
		
		return bdplQuery;
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
