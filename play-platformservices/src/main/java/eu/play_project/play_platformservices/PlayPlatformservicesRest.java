package eu.play_project.play_platformservices;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices.jaxb.Query;

@Singleton
@Path("/id")
public class PlayPlatformservicesRest implements QueryDispatchApi {

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = Constants.getProperties().getProperty("platfomservices.querydispatchapi.rest.local");

    private final HttpServer server;
	private DcepManagmentApi dcepManagmentApi;

	public PlayPlatformservicesRest() {
        final ResourceConfig rc = new ResourceConfig().registerClasses(PlayPlatformservicesRest.class);

        // uncomment the following line if you want to enable
        // support for JSON on the service (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml)
        // --
        // rc.addBinder(org.glassfish.jersey.media.json.JsonJaxbBinder);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        this.server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
	}

	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Override
	public String registerQuery(@PathParam("id") String queryId, String epSparqlQuery)
			throws QueryDispatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@DELETE
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Override
	public void unregisterQuery(@PathParam("id") String queryId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public QueryDetails analyseQuery(String queryId, String query)
			throws QueryDispatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getRegisteredQuery(@PathParam("id") String queryId)
			throws QueryDispatchException {
		// TODO Auto-generated method stub
		return null;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<Query> getRegisteredQueries() {
		Map<String, EpSparqlQuery> queries = dcepManagmentApi
				.getRegisteredEventPatterns();
		// TODO Auto-generated method stub
		return null;
	}

    public void destroy() {
    	this.server.stop();
    }

	public void setDcepManagement(DcepManagmentApi serverItf) {
		this.dcepManagmentApi = serverItf;
	}
}
