<<<<<<< HEAD
package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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

import com.hp.hpl.jena.graph.Node;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisLocalConfig;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class DcepTest implements Serializable {

	private static final long serialVersionUID = 1L;
	private static DistributedEtalisTestApi distributedEtalisTestApi;
	private static DcepManagmentApi dcepManagmentApi = null;
	private static PublishApiSubscriber subscriber = null;
	private static ConfigApi configApi = null;
	static Component root;

	/**
	 * Instantiate DCEP component and check if you get a reference to PublishApi
	 * and ManagementApi.
	 */
	@Test
	public void testInstantiateDcepComponent() {

		try {
			InstantiateDcepComponent();
		} catch (IllegalLifeCycleException e) {
			fail("Could not generate ETALIS Component" + e.getMessage());
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			fail("Could not generate ETALIS Component " + e.getMessage());
		} catch (ADLException e) {
			fail("Could not generate ETALIS Component" + e.getMessage());
			e.printStackTrace();
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (distributedEtalisTestApi == null || dcepManagmentApi == null) {
			fail("No DCEP Component");
		}

	}

	@Test
	public void registerPattern() {
		if (distributedEtalisTestApi == null) {
			fail("No DCEP component instantiated");
		} else {
			dcepManagmentApi
					.registerEventPattern(new EpSparqlQuery(
							new QueryDetails("queryId42"),
							"complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1), (xpath(element(sparqlFilter, [keyWord=O], []), //sparqlFilter(contains(@keyWord,'42')), _)))"));
			// "complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1))"));
		}
	}

	@Test
	public void pushQuadEvents() {
		if (distributedEtalisTestApi == null) {
			fail("No DCEP component instantiated");
		} else {
			for (int i = 0; i < 2; i++) {
				ArrayList<Quadruple> list = new ArrayList<Quadruple>();
				Quadruple event = new Quadruple(Node.createURI("id4710"),
						Node.createURI("http://play-project.eu/Karlsruhe"),
						Node.createURI("http://play-project.eu/is/CepResult"),
						Node.createURI("http://play-project.eu/42"));
				list.add(event);
				try {
					distributedEtalisTestApi.publish(new CompoundEvent(list));
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dcepManagmentApi.unregisterEventPattern("queryId42");
			}
		}
	}

	@Test
	public void checkComplexEvents() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Quadruple expectedResults = new Quadruple(
				Node.createURI("http://events.event-processing.org/ids/id4710"),
				Node.createURI("http://play-project.eu/Karlsruhe"), Node
						.createURI("http://play-project.eu/is/CepResult"), Node
						.createURI("http://play-project.eu/42"));

		if (subscriber.getComplexEvents() != null) {
			assertTrue(subscriber.getComplexEvents().get(0).getQuadruples()
					.get(4).equals(expectedResults));
		} else {
			System.out
					.println("ERROR: No complex events in test 'checkComplexEvents()'.");
			fail();
		}

	}

	//@Test
	public void shutDownComponents() {
		
		// Stop is rcursive...
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			// Terminate is not recursive:

			GCM.getGCMLifeCycleController(root).terminateGCMComponent();
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void InstantiateDcepComponent()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		// CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue("proactive.java.policy");
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(System
				.getProperty("user.dir")
				+ "\\src\\main\\resources\\proactive.java.policy");
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

		root = (Component) factory.newComponent("DistributedEtalis", context);
		GCM.getGCMLifeCycleController(root).startFc();

		distributedEtalisTestApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root
				.getFcInterface("DistributedEtalisTestApi"));

		configApi = ((eu.play_project.dcep.distributedetalis.api.ConfigApi) root
				.getFcInterface("ConfigApi"));
		configApi.setConfig(new DetalisLocalConfig());

		dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root
				.getFcInterface("DcepManagmentApi"));

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

}
=======
package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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

import com.hp.hpl.jena.graph.Node;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisLocalConfig;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class DcepTest implements Serializable {

	private static final long serialVersionUID = 1L;
	private static DistributedEtalisTestApi distributedEtalisTestApi;
	private static DcepManagmentApi dcepManagmentApi = null;
	private static PublishApiSubscriber subscriber = null;
	private static ConfigApi configApi = null;
	static Component root;

	/**
	 * Instantiate DCEP component and check if you get a reference to PublishApi
	 * and ManagementApi.
	 */
	@Test
	public void testInstantiateDcepComponent() {

		try {
			InstantiateDcepComponent();
		} catch (IllegalLifeCycleException e) {
			fail("Could not generate ETALIS Component" + e.getMessage());
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			fail("Could not generate ETALIS Component " + e.getMessage());
		} catch (ADLException e) {
			fail("Could not generate ETALIS Component" + e.getMessage());
			e.printStackTrace();
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (distributedEtalisTestApi == null || dcepManagmentApi == null) {
			fail("No DCEP Component");
		}

	}

	@Test
	public void registerPattern() {
		if (distributedEtalisTestApi == null) {
			fail("No DCEP component instantiated");
		} else {
			dcepManagmentApi
					.registerEventPattern(new EpSparqlQuery(
							new QueryDetails("queryId42"),
							"complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1), (xpath(element(sparqlFilter, [keyWord=O], []), //sparqlFilter(contains(@keyWord,'42')), _)))"));
			// "complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1))"));
		}
	}

	@Test
	public void pushQuadEvents() {
		if (distributedEtalisTestApi == null) {
			fail("No DCEP component instantiated");
		} else {
			for (int i = 0; i < 2; i++) {
				ArrayList<Quadruple> list = new ArrayList<Quadruple>();
				Quadruple event = new Quadruple(Node.createURI("id4710"),
						Node.createURI("http://play-project.eu/Karlsruhe"),
						Node.createURI("http://play-project.eu/is/CepResult"),
						Node.createURI("http://play-project.eu/42"));
				list.add(event);
				try {
					distributedEtalisTestApi.publish(new CompoundEvent(list));
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dcepManagmentApi.unregisterEventPattern("queryId42");
			}
		}
	}

	@Test
	public void checkComplexEvents() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Quadruple expectedResults = new Quadruple(
				Node.createURI("http://events.event-processing.org/ids/id4710"),
				Node.createURI("http://play-project.eu/Karlsruhe"), Node
						.createURI("http://play-project.eu/is/CepResult"), Node
						.createURI("http://play-project.eu/42"));

		if (subscriber.getComplexEvents() != null) {
			assertTrue(subscriber.getComplexEvents().get(0).getQuadruples()
					.get(4).equals(expectedResults));
		} else {
			System.out
					.println("ERROR: No complex events in test 'checkComplexEvents()'.");
			fail();
		}

	}

	//@Test
	public void shutDownComponents() {
		
		// Stop is rcursive...
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			// Terminate is not recursive:

			GCM.getGCMLifeCycleController(root).terminateGCMComponent();
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void InstantiateDcepComponent()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		// CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue("proactive.java.policy");
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(System
				.getProperty("user.dir")
				+ "\\src\\main\\resources\\proactive.java.policy");
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

		root = (Component) factory.newComponent("DistributedEtalis", context);
		GCM.getGCMLifeCycleController(root).startFc();

		distributedEtalisTestApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root
				.getFcInterface("DistributedEtalisTestApi"));

		configApi = ((eu.play_project.dcep.distributedetalis.api.ConfigApi) root
				.getFcInterface("ConfigApi"));
		configApi.setConfig(new DetalisLocalConfig());

		dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root
				.getFcInterface("DcepManagmentApi"));

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

}
>>>>>>> 1801c9c1039ae3f26827d175e7ad869de92b7c3a
