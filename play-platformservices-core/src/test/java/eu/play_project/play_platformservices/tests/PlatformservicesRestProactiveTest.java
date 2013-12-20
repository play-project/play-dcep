package eu.play_project.play_platformservices.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.play_commons.constants.Pattern;
import eu.play_project.play_platformservices.PlayPlatformservicesRest;
import eu.play_project.play_platformservices.api.QueryDetails;

public class PlatformservicesRestProactiveTest {
    
	private Component root;
	private Client client;
	private WebTarget targetId;
	
	/**
	 * Start Platformservices server
	 */
	@Before
	public void setup() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		Factory factory = FactoryFactory.getFactory();
		HashMap<String, Object> context = new HashMap<String, Object>();
		
		root = ProActiveHelpers.newComponent("PlatformServicesTest");
		GCM.getGCMLifeCycleController(root).startFc();
		
		client = ClientBuilder.newClient();
		targetId = client.target(PlayPlatformservicesRest.BASE_URI).path(Pattern.PATTERN_PATH);
	}
	
	@Test
	public void testAnalyseQuery() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		String queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("play-bdpl-crisis-01a-radiation.eprq"), StandardCharsets.UTF_8);
		Response response;
	    String queryId = "0001";
	    
	    // Analyse a query accepting JSON response
	    response = targetId.path(queryId).path("analyse").request(MediaType.APPLICATION_JSON).post(Entity.text(queryString));
	    assertEquals(200, response.getStatus());
	    assertEquals(queryId, response.readEntity(QueryDetails.class).getQueryId());

	    // Analyse a query accepting XML response
	    response = targetId.path(queryId).path("analyse").request(MediaType.APPLICATION_XML).post(Entity.text(queryString));
	    assertEquals(200, response.getStatus());
	    assertEquals(queryId, response.readEntity(QueryDetails.class).getQueryId());
	}
       
	/**
	 * Stop server
	 */
   	@After
   	public void destroy() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
   		client.close();
		GCM.getGCMLifeCycleController(root).stopFc();
		GCM.getGCMLifeCycleController(root).terminateGCMComponent();
		
	}
	
}
