package eu.play_project.dcep.distributedetalis.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;

import org.etsi.uri.gcm.util.GCM;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.NodeFactory;

import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.api.DcepMonitoringApi;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class DEtalisTests implements Serializable {

	private static final long serialVersionUID = 100L;
	private static DistributedEtalisTestApi distributedEtalisTestApi;
	private static DcepManagmentApi dcepManagmentApi = null;
	private static PublishApiSubscriber subscriber = null;
	private static ConfigApi configApi = null;
	static Component root;
	private final Logger logger = LoggerFactory.getLogger(DEtalisTests.class);
	DcepMonitoringApi dEtalis;


	@Test
	public void basicDetalisComponentTest() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, DistributedEtalisException, InterruptedException, DcepManagementException {
		
		/*
		 *  Check if you get a reference to PublishApi and ManagementApi:
		 */
		if (distributedEtalisTestApi == null || dcepManagmentApi == null) {
			fail("No DCEP Component");
		}

		/*
		 *  Register a pattern:
		 */
		logger.info("Register pattern.");
		
		QueryDetails qd = new QueryDetails("queryId42");
		qd.setRdfDbQueries(new ArrayList<String>());
		
		BdplQuery bdpl = BdplQuery.nonValidatingBuilder()
				.details(qd)
				.ele("complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1), (xpath(element(sparqlFilter, [keyWord=O], []), //sparqlFilter(contains(@keyWord,'42')), _)))")
				//.ele("complex(CEID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1),random(1000000, 9000000, CEID1))")
				.bdpl("")
				.build();
		
		dcepManagmentApi.registerEventPattern(bdpl);
						
		/*
		 * Push an event:
		 */
		logger.info("Push events");
		Quadruple event = new Quadruple(NodeFactory.createURI("id4710"),
				NodeFactory.createURI("http://play-project.eu/Karlsruhe"),
				NodeFactory.createURI("http://play-project.eu/is/CepResult"),
				NodeFactory.createURI("http://play-project.eu/42"));

		ArrayList<Quadruple> list = new ArrayList<Quadruple>();
		list.add(event);
		CompoundEvent ce = new CompoundEvent(list);
		logger.info("SENT: ====================================\n{}", ce);

		distributedEtalisTestApi.publish(ce);

		Thread.sleep(30000);

		/*
		 * Check results:
		 */
		logger.info("Check results.");
		Quadruple expectedResult = new Quadruple(NodeFactory.createURI("http://events.event-processing.org/ids/id4710"),
				NodeFactory.createURI("http://play-project.eu/Karlsruhe"),
				NodeFactory.createURI("http://play-project.eu/is/CepResult"),
				NodeFactory.createURI("http://play-project.eu/42"));

		CompoundEvent result = subscriber.getComplexEvents().get(0);
		logger.debug("ACTUAL: {}", result.get(4).toString());
		logger.debug("TARGET: {}", expectedResult.toString());
		assertEquals(expectedResult, subscriber.getComplexEvents().get(0).get(4));
		
		dcepManagmentApi.unregisterEventPattern(bdpl.getDetails().getQueryId());
	}

	@After
	public void shutDownComponents() {
		
		logger.info("Terminate components");
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			GCM.getGCMLifeCycleController(root).terminateGCMComponent();
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Before
	public void instantiateDcepComponent()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException, DistributedEtalisException {

		// GCMApplication gcma = PAGCMDeployment.loadApplicationDescriptor(
		// new
		// URL("file:/"+System.getProperty("user.dir")+"/src/main/resources/applicationDescriptor.xml"));
		// gcma.startDeployment();
		// gcma.waitReady();
		//
		// context.put("deployment-descriptor", gcma);

		root = ProActiveHelpers.newComponent("DistributedEtalis");
		GCM.getGCMLifeCycleController(root).startFc();

		distributedEtalisTestApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root
				.getFcInterface(DistributedEtalisTestApi.class.getSimpleName()));

		configApi = ((eu.play_project.dcep.distributedetalis.api.ConfigApi) root.getFcInterface(ConfigApi.class.getSimpleName()));

		configApi.setConfig(new DetalisConfigLocal("play-epsparql-clic2call-plus-tweet-historical-data.trig"));

		dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root.getFcInterface(DcepManagmentApi.class.getSimpleName()));
		
		dEtalis = ((eu.play_project.dcep.api.DcepMonitoringApi) root.getFcInterface(DcepMonitoringApi.class.getSimpleName()));

		// Subscribe to get complex events.
		try {
			subscriber = PAActiveObject.newActive(PublishApiSubscriber.class,
					new Object[] {});
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}

		distributedEtalisTestApi.attach(subscriber);
	}
}