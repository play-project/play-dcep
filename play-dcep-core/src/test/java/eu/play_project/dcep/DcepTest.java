package eu.play_project.dcep;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.NodeException;

import com.hp.hpl.jena.graph.NodeFactory;

import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class DcepTest implements Serializable {

	private static final long serialVersionUID = 100L;
	public static SimplePublishApi dcepPublishApi;
	public static eu.play_project.dcep.api.DcepManagmentApi dcepManagmentApi;
	public static DistributedEtalisTestApi dcepTestApi;


	@Test
	public void InstantiateDcepComponentTest() {

		try {
			InstantiateDcepComponent();
		} catch (IllegalLifeCycleException e) {
			fail("Could not generate DCEP Component" + e.getMessage());
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			fail("Could not generate DCEP Component. No SuchInterface: " + e.getMessage());
			e.printStackTrace();
		} catch (ADLException e) {
			fail("Could not generate DCEP Component" + e.getMessage());
			e.printStackTrace();
		}

		if (dcepPublishApi == null) {
			fail("No DCEP Component");
		} else {
			for (int i = 0; i < 10; i++) {
				//assertTrue(dcepPublishApi.mirror("a").equals("a"));
			}
		}

	}



	//	@Test
	public void pushEvents() throws DcepManagementException {

		PublishApiSubscriber subscriber =null;
		try {
			subscriber = PAActiveObject.newActive(PublishApiSubscriber.class, new Object[] {});
		} catch (ActiveObjectCreationException e1) {
			e1.printStackTrace();
		} catch (NodeException e1) {
			e1.printStackTrace();
		}

		//Subscribe for new events.
		dcepTestApi.attach(subscriber);

		//dcepTestApi.setEcConnectionManager(new EcConnectionMangerLocal());

		BdplQuery bdplQuery = BdplQuery.builder()
			.details(new QueryDetails("queryId42"))
			.ele("complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1))")
			.bdpl("")
			.constructTemplate(new QueryTemplateImpl())
			.historicalQueries(new LinkedList<HistoricalQuery>())
			.build();
			
		//Register pattern
		dcepManagmentApi.registerEventPattern(bdplQuery);

		if (dcepPublishApi == null) {
			fail("No DCEP component instantiated");
		} else {
			for (int i = 0; i < 30; i++) {
				List<Quadruple> quadruple = new ArrayList<Quadruple>();
				quadruple
				.add(new Quadruple(
						NodeFactory.createURI("ab" + Math.random()),
						NodeFactory.createURI("http://events.event-processing.org/ids/e2#event"),
						NodeFactory.createURI("http://events.event-processing.org/ids/endTime"),
						NodeFactory.createURI("\"2011-08-24T12:42:01.011Z\"^^http://www.w3.org/2001/XMLSchema#dateTime")));
				dcepPublishApi.publish(new CompoundEvent(quadruple));
			}
		}

		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//assertTrue(subscriber.getComplexEvents().get(0).getGraph().toString().equals("http://events.event-processing.org/ids/2620:0:1C11:e::100") ); //&&
		//	subscriber.getComplexEvents().size()==2);
	}

	
	public static void InstantiateDcepComponent()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
		.setValue("proactive.java.policy");
		// CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(System.getProperty("user.dir")+
		// "/proactive.java.policy");
		CentralPAPropertyRepository.GCM_PROVIDER
		.setValue("org.objectweb.proactive.core.component.Fractive");
		// setProAktiveHome();

		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		// GCMApplication gcma = PAGCMDeployment.loadApplicationDescriptor(
		// new
		// URL("file:/"+System.getProperty("user.dir")+"/src/main/resources/applicationDescriptor.xml"));
		// gcma.startDeployment();
		// gcma.waitReady();
		//
		// context.put("deployment-descriptor", gcma);

		Component root = (Component) factory.newComponent("StandAloneDCEP", context);
		GCM.getGCMLifeCycleController(root).startFc();

		dcepPublishApi = ((SimplePublishApi) root
				.getFcInterface(SimplePublishApi.class.getSimpleName()));

		dcepManagmentApi = ((DcepManagmentApi) root
				.getFcInterface(DcepManagmentApi.class.getSimpleName()));

		dcepTestApi = ((DistributedEtalisTestApi) root
				.getFcInterface(DistributedEtalisTestApi.class.getSimpleName()));

	}

	/**
	 * Sets the value of proactive.home if not already set Hack from:
	 * http://kickjava
	 * .com/src/org/objectweb/proactive/core/config/ProActiveConfiguration
	 * .java.htm
	 */
	private void setProAktiveHome() {
		File file = null;
		if (System.getProperty("proactive.home") == null) {
			String location = ProActiveConfiguration.class.getResource(
					"ProActiveConfiguration.class").getPath();
			location = location.split("/proactive-")[0].toString();

			try {
				file = new File(location.split("file:")[1]);

				String proactivehome = file.getCanonicalPath();
				System.setProperty("proactive.home", proactivehome);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("ProaktiveHome not found");
			}
		}
	}


	public void getEvent(CompoundEvent event){
		System.out.println("OK i got an event :=) " +  event.getGraph());
	}
}
