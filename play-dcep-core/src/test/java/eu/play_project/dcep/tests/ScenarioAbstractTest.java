package eu.play_project.dcep.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.etsi.uri.gcm.util.GCM;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.Syntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.QueryDispatchApi;


/**
 * Abstract test class dealing with initiation of the platform before each test.
 * Instatiating the platform is done with {@link BeforeClass} because the
 * native Prolog engine can be instatiated only once.
 */
public class ScenarioAbstractTest {
	public static QueryDispatchApi queryDispatchApi;
	public static DistributedEtalisTestApi testApi;
	boolean start = false;
	public static Component root;
	public static boolean test;
	public static final Logger logger = LoggerFactory.getLogger(ScenarioAbstractTest.class);
	
	@BeforeClass
	public static void instantiatePlayPlatform()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		root = ProActiveHelpers.newComponent("PsDcepComponent");
		GCM.getGCMLifeCycleController(root).startFc();

		queryDispatchApi = (QueryDispatchApi) root.getFcInterface(QueryDispatchApi.class.getSimpleName());
		testApi = (DistributedEtalisTestApi) root.getFcInterface(DistributedEtalisTestApi.class.getSimpleName());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void terminatePlayPlatform() {
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
	
	public static String loadSparqlQuery(String queryFile) throws IOException{
		return IOUtils.toString(ScenarioAbstractTest.class.getClassLoader().getResourceAsStream(queryFile), StandardCharsets.UTF_8);
	}
	
	
	public static Model loadEvent(String rdfFile, Syntax rdfSyntax) throws ModelRuntimeException, IOException{
		ModelSet event = EventHelpers.createEmptyModelSet();
		event.readFrom(ScenarioMyGreenServicesTest.class.getClassLoader().getResourceAsStream(rdfFile), rdfSyntax);
		return event.getModels().next();
	}

}