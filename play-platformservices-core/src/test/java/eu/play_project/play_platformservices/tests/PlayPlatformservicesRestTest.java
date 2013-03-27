package eu.play_project.play_platformservices.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.etsi.uri.gcm.util.GCM;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import eu.play_project.play_platformservices.PlayPlatformservicesRest;

public class PlayPlatformservicesRestTest {
    
	private Component root;
	
	/**
	 * Start Platformservices server
	 */
	@Before
	public void setup() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
		.setValue("proactive.java.policy");

		CentralPAPropertyRepository.GCM_PROVIDER
		.setValue("org.objectweb.proactive.core.component.Fractive");
		
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();
		
		root = (Component) factory.newComponent("PlatformServicesTest", context);
		GCM.getGCMLifeCycleController(root).startFc();
	}
	
	/**
	 * Start client and send some requests
	 */
	@Test
	public void testPlayPlatformservicesRest() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		String file = "play-bdpl-crisis-01a-radiation.eprq";
		String query = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(file), "UTF-8");
		
	    Client client = ClientBuilder.newClient();
	    Response response = client.target(UriBuilder.fromUri(PlayPlatformservicesRest.BASE_URI).path("id")).request(MediaType.APPLICATION_JSON).get();
	    assertEquals(500, response.getStatus());
	}
       
	/**
	 * Stop server
	 */
   	@After
   	public void destroy() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		GCM.getGCMLifeCycleController(root).stopFc();
		GCM.getGCMLifeCycleController(root).terminateGCMComponent();
		
	}
	
}
