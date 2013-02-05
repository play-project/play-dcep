package eu.play_project.dcep;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.*; // Because of Maven, to find fractal file
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.QueryDetails;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.TurnActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.NodeException;


import com.hp.hpl.jena.graph.Node;
import com.jtalis.core.JtalisContextImpl;
import com.jtalis.core.plengine.JPLEngineWrapper;
import com.jtalis.core.plengine.PrologEngineWrapper;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;

import static org.junit.Assert.*;

public class DcepTest implements Serializable {

	private static final long serialVersionUID = 1L;
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
	public void pushEvents() {

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

		//Register pattern
		dcepManagmentApi.registerEventPattern(new EpSparqlQuery(
				new QueryDetails("queryId42"),
				"complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1))"));
		
		if (dcepPublishApi == null) {
			fail("No DCEP component instantiated");
		} else {
			for (int i = 0; i < 30; i++) {
				List<Quadruple> quadruple = new ArrayList<Quadruple>();
				quadruple
						.add(new Quadruple(
							Node.createURI("ab" + Math.random()),
							Node.createURI("http://events.event-processing.org/ids/e2#event"),
							Node.createURI("http://events.event-processing.org/ids/endTime"),
							Node.createURI("\"2011-08-24T12:42:01.011Z\"^^http://www.w3.org/2001/XMLSchema#dateTime")));
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
				.getFcInterface("SimplePublishApi"));
		
		dcepManagmentApi = ((DcepManagmentApi) root
				.getFcInterface("DcepManagmentApi"));
		
		dcepTestApi = ((DistributedEtalisTestApi) root
				.getFcInterface("DistributedEtalisTestApi"));
		
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
