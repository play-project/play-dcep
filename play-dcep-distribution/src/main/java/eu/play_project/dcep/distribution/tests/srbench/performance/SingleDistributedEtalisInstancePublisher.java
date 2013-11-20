package eu.play_project.dcep.distribution.tests.srbench.performance;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;
import org.slf4j.LoggerFactory;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;

/**
 * Connect to a DistributedEtalis instance and use the api(s).
 * @author Stefan Obermeier
 *
 */
public class SingleDistributedEtalisInstancePublisher {

	private static List<DistributedEtalisTestApi> testApis =  new LinkedList<DistributedEtalisTestApi>();
	private static List<DcepManagmentApi> managementApis =  new LinkedList<DcepManagmentApi>();
	private static List<ConfigApi> configApis =   new LinkedList<ConfigApi>();
	static int patternIdCounter = 0;
	private static Logger logger;
	
	public static void main(String[] args) throws RemoteException,
			NotBoundException, Exception {
		
		logger = LoggerFactory.getLogger(SingleDistributedEtalisInstancePublisher.class);
		
		for (int i = 0; i < args.length; i++) {
			// Connect to DistributedEtalis instance 1.
			PAComponentRepresentative root1 = Fractive.lookup((URIBuilder.buildURI(args[i], "dEtalis", "pnp", Integer.parseInt(DcepConstants.getProperties().getProperty("dcep.proactive.pnp.port"))).toString()));
			configApis.add(i, ((eu.play_project.dcep.distributedetalis.api.ConfigApi) root1.getFcInterface(ConfigApi.class.getSimpleName())));
			configApis.get(i).setConfig(new DetalisConfigLocal("play-epsparql-clic2call-historical-data.trig"));
			testApis.add(i, ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root1.getFcInterface(DistributedEtalisTestApi.class.getSimpleName())));
			managementApis.add(i, ((eu.play_project.dcep.api.DcepManagmentApi) root1.getFcInterface(DcepManagmentApi.class.getSimpleName())));
		}

		BdplQuery q = createCepQuery(("p1" + (++patternIdCounter + Math.random())) , getSparqlQueries("benchmarks/srbench/q3.eprq"));
		
		// Register queries.
		managementApis.get(0).registerEventPattern(q);
//		try {
//			for (DcepManagmentApi managementApi : managementApis) {
//				managementApi.registerEventPattern(q);
//			}
//		} catch (Exception e) {
//			logger.error("Error while registering pattern. Message: {} ", e.getMessage());
//		} catch (Throwable e) {
//			logger.error("Error while registering pattern. Message: {} ", e.getMessage());
//		}

		// Start publishing events.
		new EventProducerThread(20002, 6, testApis);
	}

	private static BdplQuery createCepQuery(String queryId, String query)
			throws QueryDispatchException {
		// Parse query
		Query q;
		try {
			q = QueryFactory.create(query, Syntax.syntaxBDPL);
		} catch (com.hp.hpl.jena.query.QueryException e) {
			throw new QueryDispatchException(e.getMessage());
		}
		EleGenerator eleGenerator = new EleGeneratorForConstructQuery();
		
		// Generate CEP-language
		eleGenerator.setPatternId(queryId);
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
