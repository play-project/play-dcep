package eu.play_project.dcep;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.etsi.uri.gcm.util.GCM;
import org.junit.After;
import org.junit.Before;
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
import org.objectweb.proactive.core.node.NodeException;
import org.ontoware.rdf2go.impl.jena.ModelImplJena;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

public class CommonsPatternTest2 {

	public static QueryDispatchApi queryDispatchApi;
	public static DistributedEtalisTestApi testApi;
	boolean start = false;
	static Component root;
	public static boolean test;
	private final Logger logger = LoggerFactory.getLogger(CommonsPatternTest2.class);
	
	@Test
	public void testThreeMissedCalls() throws QueryDispatchException, IOException {
		
		String queryString;

		// Get query.
		queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("patterns/play-bdpl-telco-orange-eval-v3-full.eprq"));

		// Compile query
		queryDispatchApi.registerQuery("example1", queryString);

		//Subscribe to get complex events.
		SimplePublishApiSubscriber subscriber = null;
		try {
			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}

		testApi.attach(subscriber);
	
		logger.info("Publish events");
		for (int i = 0; i < 5; i++) {
			Model call0 = RDFDataMgr.loadModel("events/call0.nq", RDFLanguages.NQ);
			testApi.publish(EventCloudHelpers.toCompoundEvent(new ModelImplJena(new URIImpl(call0.getGraph().toString()), call0)));
			Model call1 = RDFDataMgr.loadModel("events/call1.nq", RDFLanguages.NQ);
			testApi.publish(EventCloudHelpers.toCompoundEvent(new ModelImplJena(new URIImpl(call1.getGraph().toString()), call1)));
			Model call2 = RDFDataMgr.loadModel("events/call2.nq", RDFLanguages.NQ);
			testApi.publish(EventCloudHelpers.toCompoundEvent(new ModelImplJena(new URIImpl(call2.getGraph().toString()), call2)));
		}

		// Wait
		delay();

		assertTrue(subscriber.getComplexEvents().size()==1);
	}
	
	@Before
	public void instantiatePlayPlatform()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
				.setValue("proactive.java.policy");

		CentralPAPropertyRepository.GCM_PROVIDER
				.setValue("org.objectweb.proactive.core.component.Fractive");

		
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();

		root = (Component) factory.newComponent("PsDcepComponent", context);
		GCM.getGCMLifeCycleController(root).startFc();

		queryDispatchApi = ((QueryDispatchApi) root.getFcInterface(QueryDispatchApi.class.getSimpleName()));
		testApi = (DistributedEtalisTestApi) root.getFcInterface(DistributedEtalisTestApi.class.getSimpleName());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void terminatPlayPlatform() {
		// Stop and terminate GCM Components
		try {
			GCM.getGCMLifeCycleController(root).stopFc();
			// Terminate all subcomponents.
			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
			 }
	
		} catch (IllegalLifeCycleException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
	}


	private void delay(){
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
