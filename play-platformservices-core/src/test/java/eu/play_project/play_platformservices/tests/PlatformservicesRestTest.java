package eu.play_project.play_platformservices.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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
	public void setup() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		platformservice = new PlayPlatformservicesRest(new MockPlatformservice());
		
		client = ClientBuilder.newClient();
		targetId = client.target(PlayPlatformservicesRest.BASE_URI).path(Pattern.PATTERN_PATH);
	}
	
	/**
	 * Start client and send some requests
	 */
	@Test
	public void testPlayPlatformservicesRest() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, IOException {
		String queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("play-bdpl-crisis-01a-radiation.eprq"), "UTF-8");
		Response response;
	    String queryId = "0001";
		
	    // Check not (yet) existing query
	    response = targetId.path(queryId).request(MediaType.APPLICATION_JSON).get();
	    assertEquals(404, response.getStatus());

	    // Put a query
	    response = targetId.path(queryId).request(MediaType.APPLICATION_JSON).put(Entity.text(queryString));
	    assertEquals(200, response.getStatus());

	    // Get it as JSON
	    response = targetId.path(queryId).request(MediaType.APPLICATION_JSON).get();
	    assertEquals(queryId, response.readEntity(Query.class).id);
	    assertEquals(200, response.getStatus());

	    // Get it as XML
	    response = targetId.path(queryId).request(MediaType.APPLICATION_XML).get();
	    assertEquals(queryId, response.readEntity(Query.class).id);
	    assertEquals(200, response.getStatus());

	    // Get it as Text
	    response = targetId.path(queryId).request(MediaType.TEXT_PLAIN).get();
	    assertEquals(200, response.getStatus());

	    // Get all queries, should be 1
	    response = targetId.request(MediaType.APPLICATION_JSON).get();
	    assertEquals(1, response.readEntity(new GenericType<List<Query>>(){}).size());
	    assertEquals(200, response.getStatus());

	    // Post a query
		queryString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("play-bdpl-crisis-01b-radiationincrease.eprq"), "UTF-8");
	    response = targetId.request(MediaType.APPLICATION_JSON).post(Entity.text(queryString));
	    assertEquals(201, response.getStatus());

	    // Get all queries, should be 2
	    response = targetId.request(MediaType.APPLICATION_JSON).get();
	    assertEquals(2, response.readEntity(new GenericType<List<Query>>(){}).size());
	    assertEquals(200, response.getStatus());

	    // Delete one query
	    response = targetId.path(queryId).request().delete();
	    assertEquals(204, response.getStatus());
	    
	    // Get all queries, should be 1
	    response = targetId.request(MediaType.APPLICATION_JSON).get();
	    assertEquals(1, response.readEntity(new GenericType<List<Query>>(){}).size());
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
		public String registerQuery(String queryId, String epSparqlQuery)
				throws QueryDispatchException {
			this.registeredQueries.put(queryId, new Query(queryId, epSparqlQuery));
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
