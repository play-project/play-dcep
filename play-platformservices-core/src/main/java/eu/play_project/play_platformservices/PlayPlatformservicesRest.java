package eu.play_project.play_platformservices;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

@Singleton
@Path("/id")
public class PlayPlatformservicesRest implements QueryDispatchApi {

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = Constants.getProperties().getProperty("platfomservices.querydispatchapi.rest.local");

    private final HttpServer server;
	private final QueryDispatchApi playPlatformservices;

	public PlayPlatformservicesRest(QueryDispatchApi playPlatformservices) {
 
		final ResourceConfig rc = new ResourceConfig();
		rc.register(this);
		rc.register(new MoxyJsonFeature());
		rc.register(new MoxyXmlFeature());
		rc.registerInstances(new JsonMoxyConfigurationContextResolver());

       this.playPlatformservices = playPlatformservices;

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        this.server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
	}

	@PUT
	@Path("{id}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
	@Override
	public String registerQuery(@PathParam("id") String queryId, String epSparqlQuery)
			throws QueryDispatchException {
		return this.playPlatformservices.registerQuery(queryId, epSparqlQuery);
	}

	@DELETE
	@Path("{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
	@Override
	public void unregisterQuery(@PathParam("id") String queryId) {
		this.playPlatformservices.unregisterQuery(queryId);
	}

	@POST
	@Path("analyse")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Override
	public QueryDetails analyseQuery(@HeaderParam("id") String queryId, String query)
			throws QueryDispatchException {
		return this.playPlatformservices.analyseQuery(queryId, query);
	}

	@GET
	@Path("{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
	@Override
	public eu.play_project.play_platformservices.jaxb.Query getRegisteredQuery(@PathParam("id") String queryId)
			throws QueryDispatchException {
		return this.playPlatformservices.getRegisteredQuery(queryId);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Override
	public List<eu.play_project.play_platformservices.jaxb.Query> getRegisteredQueries() {
		return this.playPlatformservices.getRegisteredQueries();
	}

    public void destroy() {
    	this.server.stop();
    }
	
	@Provider
	private final static class JsonMoxyConfigurationContextResolver implements
			ContextResolver<MoxyJsonConfig> {

		@Override
		public MoxyJsonConfig getContext(Class<?> objectType) {
			final MoxyJsonConfig configuration = new MoxyJsonConfig();

			Map<String, String> namespacePrefixMapper = new HashMap<String, String>(
					1);
			namespacePrefixMapper.put(
					"http://www.w3.org/2001/XMLSchema-instance", "xsi");

			configuration.setNamespacePrefixMapper(namespacePrefixMapper);
			configuration.setNamespaceSeparator(':');

			return configuration;
		}
	}
}
