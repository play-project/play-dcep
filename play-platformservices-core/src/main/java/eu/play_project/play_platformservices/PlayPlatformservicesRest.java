package eu.play_project.play_platformservices;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_commons.constants.Pattern;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices.jaxb.Query;

/**
 * The PLAY REST Web Service to manage event patterns. See
 * {@linkplain PlayPlatformservices} for the corresponding SOAP service.
 * 
 * N.B.: New event patterns (i.e. queries) are registered by using HTTP PUT on
 * the respective URI using a caller-specified unique URI. There is currently no
 * way (e.g. using HTTP POST) where the unique ID does not need to be known
 * before the request.
 * 
 * @author Roland Stühmer
 */
@Singleton
@Path(Pattern.PATTERN_PATH)
@Consumes(MediaType.TEXT_PLAIN)
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PlayPlatformservicesRest implements QueryDispatchApi {

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = Constants.getProperties().getProperty("platfomservices.querydispatchapi.rest.local");
    
    /* Injected by Jersey */
    @Context
	private UriInfo uriInfo;
    
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
	
	/**
	 * A setter (only evailable in REST service not SOAP
	 * {@linkplain PlayPlatformservices}) to add an anonymous query without ID.
	 * A random {@linkplain UUID} will be assigned and the child-resource created.
	 */
	@POST
	@Path("/")
	public Response registerQuery(String queryString)
			throws QueryDispatchException {
		String queryId = this.playPlatformservices.registerQuery(UUID.randomUUID().toString(), queryString);
		URI uri = uriInfo.getAbsolutePathBuilder().path(queryId).build();
		return Response.created(uri).entity(queryId).build();
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
	 * A getter (only evailable in REST service not SOAP
	 * {@linkplain PlayPlatformservices}) specifically to return a String
	 * instead of a {@linkplain Query} for human-readable browsing.
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
	@Path("/")
	@Override
	public List<Query> getRegisteredQueries() {
		return this.playPlatformservices.getRegisteredQueries();
	}

    public void destroy() {
    	this.server.stop();
    }
}
