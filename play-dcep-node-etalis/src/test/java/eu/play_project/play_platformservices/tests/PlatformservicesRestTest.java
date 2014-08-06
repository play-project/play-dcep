package eu.play_project.play_platformservices.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import eu.play_project.play_commons.constants.Pattern;
import eu.play_project.play_platformservices.PlayPlatformservicesRest;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices.jaxb.Query;

public class PlatformservicesRestTest {
    
	private Client client;
	private WebTarget targetId;
	private PlayPlatformservicesRest platformservice;
	
	/**
	 * Start Platformservices server
	 */
	@Before
	public void setup() throws Exception {
		platformservice = new PlayPlatformservicesRest(new MockPlatformservice());
		
		client = ClientBuilder.newClient();
		targetId = client.target(PlayPlatformservicesRest.BASE_URI).path(Pattern.PATTERN_PATH);
	}
	
	/**
	 * Start client and send some consecutive requests
	 */
	@Test
	public void testVariousVerbs() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		String queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("play-bdpl-crisis-01a-radiation.eprq"), StandardCharsets.UTF_8);
		Response response;
	    String queryId = "0001";
		
	    // Check not (yet) existing query
	    response = targetId.path(queryId).request(MediaType.APPLICATION_JSON).get();
	    assertEquals(404, response.getStatus());

	    // Put a query
	    response = targetId.path(queryId).request(MediaType.APPLICATION_JSON).put(Entity.text(queryString));
	    assertEquals(200, response.getStatus());

	    // Put a query again
	    response = targetId.path(queryId).request(MediaType.APPLICATION_JSON).put(Entity.text(queryString));
	    assertEquals(500, response.getStatus());

	    // Get it as JSON
	    response = targetId.path(queryId).request(MediaType.APPLICATION_JSON).get();
	    assertEquals(queryId, response.readEntity(Query.class).id);
	    assertEquals(200, response.getStatus());

	    // Get all queries, should be 1
	    response = targetId.request(MediaType.APPLICATION_JSON).get();
	    assertEquals(1, response.readEntity(new GenericType<List<Query>>(){}).size());
	    assertEquals(200, response.getStatus());

	    // Post a query
		queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("play-bdpl-crisis-01b-radiationincrease.eprq"), StandardCharsets.UTF_8);
	    response = targetId.request(MediaType.APPLICATION_JSON).post(Entity.text(queryString));
	    assertEquals(201, response.getStatus());

	    // Get all queries, should be 2
	    response = targetId.request(MediaType.APPLICATION_JSON).get();
	    assertEquals(2, response.readEntity(new GenericType<List<Query>>(){}).size());
	    assertEquals(200, response.getStatus());

	    // Delete one query
	    response = targetId.path(queryId).request().delete();
	    assertEquals(204, response.getStatus());

	    // Delete the query again
	    response = targetId.path(queryId).request().delete();
	    assertEquals(204, response.getStatus());

	    // Get all queries, should be 1
	    response = targetId.request(MediaType.APPLICATION_JSON).get();
	    assertEquals(1, response.readEntity(new GenericType<List<Query>>(){}).size());
	    assertEquals(200, response.getStatus());
	}
	
	/**
	 * Start client and test various request formats
	 */
	@Test
	public void testVariousMediatypes() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		String queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("play-bdpl-crisis-01a-radiation.eprq"), StandardCharsets.UTF_8);
		Response response;

	    // Post a query via form (and get the new query location URI)
		Form form = new Form();
		form.param("queryString", queryString);
	    response = targetId.request(MediaType.APPLICATION_JSON).post(Entity.form(form));
	    assertEquals(201, response.getStatus());
	    assertNotNull(response.getLocation());
	    URI queryUri = response.getLocation();
	    WebTarget targetNew = client.target(queryUri);

	    // Get it as XML
	    response = targetNew.request(MediaType.APPLICATION_XML).get();
	    assertTrue(queryUri.toString().endsWith(response.readEntity(Query.class).id));
	    assertEquals(200, response.getStatus());

	    // Get it as Text
	    response = targetNew.request(MediaType.TEXT_PLAIN).get();
	    assertEquals(200, response.getStatus());
	    
	
	}
       
	/**
	 * Stop server
	 */
   	@After
   	public void destroy() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
   		client.close();
   		platformservice.destroy();
   	}
   	
   	class MockPlatformservice implements QueryDispatchApi {

   		private final Map<String, Query> registeredQueries = new HashMap<String, Query>();
   		
		@Override
		public String registerQuery(String queryId, String bdplQuery)
				throws QueryDispatchException {
			if (this.registeredQueries.containsKey(queryId)) {
				throw new QueryDispatchException("Query id already exists. \"{}\"" + queryId);
			}
			this.registeredQueries.put(queryId, new Query(queryId, bdplQuery));
			return queryId;
		}

		@Override
		public void unregisterQuery(String queryId) {
			this.registeredQueries.remove(queryId);
		}

		@Override
		public QueryDetails analyseQuery(String queryId, String query)
				throws QueryDispatchException {
			return new QueryDetails(queryId);
		}

		@Override
		public Query getRegisteredQuery(String queryId) {
			
			Query q = this.registeredQueries.get(queryId);
			if (q != null) {
				return q;
			}
			else {
				throw new WebApplicationException(404);
			}
		}

		@Override
		public List<Query> getRegisteredQueries() {
			return new ArrayList<Query>(registeredQueries.values());
		}
   		
   	}
	
}
