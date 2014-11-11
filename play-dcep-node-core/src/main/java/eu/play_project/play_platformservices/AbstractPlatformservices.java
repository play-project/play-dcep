package eu.play_project.play_platformservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.DcepManagementException;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

/**
 * The PLAY SOAP Web Service to manage event patterns. See
 * {@linkplain PlayPlatformservicesRest} for the corresponding RESTful service.
 * 
 * @author Roland Stühmer
 */
@WebService(
		serviceName = "QueryDispatchApi",
		portName = "QueryDispatchApiPort",
		endpointInterface = "eu.play_project.play_platformservices.api.QueryDispatchApi")
public abstract class AbstractPlatformservices implements QueryDispatchApi, Serializable {

	private static final long serialVersionUID = 100L;
	private DcepManagmentApi dcepManagmentApi;
	private boolean init = false;
	private final Logger logger = LoggerFactory.getLogger(AbstractPlatformservices.class);
	private Endpoint soapServer;
	private PlayPlatformservicesRest restServer;

	protected boolean isInitialized() {
		return init;
	}
	
	public synchronized void initialize() {
		if (!isInitialized()) {
			
			logger.info("Initialising {} component.", this.getClass().getSimpleName());
	
			/*
			 * Provide QueryDispatchApi as SOAP Webservice
			 */
			try {
				String address = Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint");
				soapServer = Endpoint.publish(address, this);
				logger.info("QueryDispatch SOAP service started at {}.", address);
			} catch (Exception e) {
				logger.error("Exception while publishing QueryDispatch SOAP Service", e);
			}
	
			/*
			 * Provide QueryDispatchApi as REST Webservice
			 */
			try {
				restServer = new PlayPlatformservicesRest(this);
	        	logger.info(String.format("QueryDispatch REST service started at %s with WADL remotely available at "
	        			+ "%s/application.wadl\n", PlayPlatformservicesRest.BASE_URI, Constants.getProperties().getProperty("platfomservices.querydispatchapi.rest")));
			} catch (Exception e) {
				logger.error("Exception while publishing QueryDispatch REST Service", e);
			}
			
			this.init = true;
		}
	}
	
	public synchronized void destroy() {
		logger.info("Terminating {} component.", this.getClass().getSimpleName());
		this.init = false;
		
		if (this.soapServer != null) {
			this.soapServer.stop();
		}
		
		if (this.restServer != null) {
			this.restServer.destroy();
		}
	}
	

	@Override
	public synchronized String registerQuery(String queryId, String query) throws QueryDispatchException {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: "
					+ this.getClass().getSimpleName());
		}

		logger.info("Registering query with ID '{}'", queryId);

		BdplQuery epQuery = createQuery(queryId, query);
		
		try {
			dcepManagmentApi.registerEventPattern(epQuery);
		} catch (Exception e) {
			String msg = String.format("Error while registering query '%s': %s: %s", queryId, e.getClass().getSimpleName(), e.getMessage());
			logger.error(msg);
			throw new QueryDispatchException(msg);
		}
		return queryId;
	}
	
	protected abstract BdplQuery createQuery(String queryId, String query)
			throws QueryDispatchException;
	
	public DcepManagmentApi getDcepManagmentApi() {
		return this.dcepManagmentApi;
	}
	
	public void setDcepManagmentApi(DcepManagmentApi dcepManagmentApi) {
		this.dcepManagmentApi = dcepManagmentApi;
	}

	@Override
	public void unregisterQuery(String queryId) {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: "
					+ this.getClass().getSimpleName());
		}
		
		logger.info("Unregistering query {}", queryId);

		this.dcepManagmentApi.unregisterEventPattern(queryId);
	}

	@Override
	public eu.play_project.play_platformservices.jaxb.Query getRegisteredQuery(String queryId)
			throws QueryDispatchException {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: "
					+ this.getClass().getSimpleName());
		}
		try {
			return new eu.play_project.play_platformservices.jaxb.Query(
					this.dcepManagmentApi.getRegisteredEventPattern(queryId));
		} catch (DcepManagementException e) {
			throw new QueryDispatchException(e.getMessage());
		}
	}

	@Override
	public List<eu.play_project.play_platformservices.jaxb.Query> getRegisteredQueries() {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: "
					+ this.getClass().getSimpleName());
		}

		List<eu.play_project.play_platformservices.jaxb.Query> results = new ArrayList<eu.play_project.play_platformservices.jaxb.Query>();

		Map<String, BdplQuery> queries = dcepManagmentApi
				.getRegisteredEventPatterns();

		for (String queryId : queries.keySet()) {
			results.add(new eu.play_project.play_platformservices.jaxb.Query(queries.get(queryId)));
		}

		return results;
	}

}
