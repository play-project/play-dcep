package eu.play_project.play_platformservices;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Roland Stühmer
 */
@Singleton
@Path(Pattern.PATTERN_PATH)
@Consumes(MediaType.TEXT_PLAIN)
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PlayPlatformservicesRest implements QueryDispatchApi {

    // Base URI the HTTP server will listen on
    public static final String BASE_URI = Constants.getProperties().getProperty("platfomservices.querydispatchapi.rest.local");

	private final Logger logger = LoggerFactory.getLogger(PlayPlatformservicesRest.class);

    /* Injected by Jersey */
    @Context
	private UriInfo uriInfo;
    
	private final QueryDispatchApi playPlatformservices;

	private final Server server;

	public PlayPlatformservicesRest(QueryDispatchApi playPlatformservices) throws Exception {
 
		final ResourceConfig rc = new ResourceConfig()
				.register(this)
				.register(MoxyJsonFeature.class);

		this.playPlatformservices = playPlatformservices;

		// create and start a new instance of the http server
		// exposing the Jersey application at BASE_URI
		server = new Server(URI.create(BASE_URI).getPort());
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		ServletHolder h = new ServletHolder(new ServletContainer(rc));
		context.addServlet(h, "/");
		server.setHandler(context);
		server.start();

	}

	@POST
	@Path("{id}/analyse")
	@Override
	public QueryDetails analyseQuery(@PathParam("id") String queryId, String queryString)
			throws QueryDispatchException {
		return this.playPlatformservices.analyseQuery(queryId, queryString);
	}

	@POST
	@Path("{id}/analyse")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public QueryDetails analyseQueryViaForm(@PathParam("id") String queryId, @FormParam("queryString") String queryString)
			throws QueryDispatchException {
		return analyseQuery(queryId, queryString);
	}
	
	/**
	 * A setter (only evailable in REST service not SOAP
	 * {@linkplain PlayPlatformservices}) to add an anonymous query without ID.
	 * A random {@linkplain UUID} will be assigned and the child-resource created.
	 */
	@POST
	public Response registerQuery(String queryString)
			throws QueryDispatchException {
		String queryId = this.playPlatformservices.registerQuery(UUID.randomUUID().toString(), queryString);
		URI uri = uriInfo.getAbsolutePathBuilder().path(queryId).build();
		return Response.created(uri).entity(queryId).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response registerQueryViaForm(@FormParam("queryString") String queryString)
			throws QueryDispatchException {
		return registerQuery(queryString);
	}

	@PUT
	@Path("{id}")
	@Override
	public String registerQuery(@PathParam("id") String queryId, String queryString)
			throws QueryDispatchException {
		return this.playPlatformservices.registerQuery(queryId, queryString);
	}

	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String registerQueryViaForm(@PathParam("id") String queryId, @FormParam("queryString") String queryString)
			throws QueryDispatchException {
		return registerQuery(queryId, queryString);
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
	@Override
	public List<Query> getRegisteredQueries() {
		return this.playPlatformservices.getRegisteredQueries();
	}

    public void destroy() {
    	if (this.server != null) {
    		try {
				this.server.stop();
			} catch (Exception e) {
				logger.error("Exception while stoppping REST server. Nothing we can do now. {}", e.getMessage());
			}
    		this.server.destroy();
    	}
    }
}
