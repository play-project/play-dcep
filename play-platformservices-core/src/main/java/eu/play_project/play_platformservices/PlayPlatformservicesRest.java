package eu.play_project.play_platformservices;

import java.net.URI;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices.jaxb.Query;

@Singleton
@Path("/patterns")
@Consumes(MediaType.TEXT_PLAIN)
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PlayPlatformservicesRest implements QueryDispatchApi {

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = Constants.getProperties().getProperty("platfomservices.querydispatchapi.rest.local");

    private final HttpServer server;
	private final QueryDispatchApi playPlatformservices;

	public PlayPlatformservicesRest(QueryDispatchApi playPlatformservices) {
 
		final ResourceConfig rc = new ResourceConfig();
		rc.register(this);

       this.playPlatformservices = playPlatformservices;

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        this.server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
	}

	@POST
	@Path("{id}/analyse")
	@Override
	public QueryDetails analyseQuery(@PathParam("id") String queryId, String queryString)
			throws QueryDispatchException {
		return this.playPlatformservices.analyseQuery(queryId, queryString);
	}

	@PUT
	@Path("{id}")
	@Override
	public String registerQuery(@PathParam("id") String queryId, String queryString)
			throws QueryDispatchException {
		return this.playPlatformservices.registerQuery(queryId, queryString);
	}

	@DELETE
	@Path("{id}")
	@Override
	public void unregisterQuery(@PathParam("id") String queryId) {
		this.playPlatformservices.unregisterQuery(queryId);
	}

	@GET
	@Path("{id}")
	@Override
	public Query getRegisteredQuery(@PathParam("id") String queryId) {
		try {
			return this.playPlatformservices.getRegisteredQuery(queryId);
		} catch (QueryDispatchException e) {
			throw new WebApplicationException(e, 404);
		}
	}

	/**
	 * Additional getter specifically to return a String instead of a
	 * {@linkplain Query}.
	 * 
	 * @param queryId
	 * @return
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getRegisteredQueryAsString(@PathParam("id") String queryId) {
		try {
			return this.playPlatformservices.getRegisteredQuery(queryId).content;
		} catch (QueryDispatchException e) {
			throw new WebApplicationException(e, 404);
		}
	}
	
	@GET
	@Override
	public List<Query> getRegisteredQueries() {
		return this.playPlatformservices.getRegisteredQueries();
	}

    public void destroy() {
    	this.server.stop();
    }
}
