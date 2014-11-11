package eu.play_project.dcep.node.connections;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.dcep.node.api.EcConnectionManager;
import eu.play_project.dcep.node.api.EcConnectionmanagerException;
import eu.play_project.dcep.node.api.SelectResults;
import eu.play_project.dcep.node.listeners.AbstractConnectionListenerRest;
import eu.play_project.dcep.node.listeners.AbstractConnectionListenerWsn;
import eu.play_project.dcep.node.listeners.DuplicateCheckingListener;
import eu.play_project.dcep.node.persistence.Persistence;
import eu.play_project.dcep.node.persistence.PersistenceException;
import eu.play_project.dcep.node.persistence.Sqlite;
import eu.play_project.dcep.node.persistence.Sqlite.SubscriptionPerCloud;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import eu.play_project.play_eventadapter.AbstractSenderRest;
import eu.play_project.play_platformservices.api.BdplQuery;
/**
 * An abstract connection manager to get real-time events from the PLAY
 * Platform. Access to historic data, however, must be implemented by extending
 * classes.
 * 
 * @author Roland Stühmer
 */
public abstract class AbstractConnectionManagerWsn<EventType> implements EcConnectionManager<EventType> {
	private final Map<String, SubscriptionUsage> subscriptions = new HashMap<String, SubscriptionUsage>();
	private final Logger logger = LoggerFactory.getLogger(AbstractConnectionManagerWsn.class);
	protected boolean init = false;
	private AbstractReceiverRest rdfReceiver;
	private AbstractSenderRest rdfSender;
	private final DcepNodeApi<EventType> dEtalis;
	private AbstractConnectionListenerWsn<EventType> wsnListener;
	private DuplicateCheckingListener<EventType> restListener;
	public static final Properties constants = DcepConstants.getProperties();
	private static final String SOAP_URI = constants.getProperty("dcep.notify.endpoint");
    private static final String REST_URI = constants.getProperty("dcep.notify.rest");
	private Service notifyReceiverSoap;
	private Server notifyReceiverRest;
	private Persistence persistence;
	
	public AbstractConnectionManagerWsn(DcepNodeApi<EventType> dEtalis) {
		this.dEtalis = dEtalis;
	}
	
	public void init(AbstractConnectionListenerWsn<EventType> wsnListener,
			AbstractConnectionListenerRest<EventType> restListener)
			throws EcConnectionmanagerException {
		
		if (init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has ALREADY been initialized.");
		}

		logger.info("Initialising {}.", this.getClass().getSimpleName());
		
		this.rdfReceiver = wsnListener.getRdfReceiver();
				
		// Use an arbitrary topic as default:
		this.rdfSender = new AbstractSenderRest(Stream.FacebookCepResults.getTopicQName()) {};

        /*
         * Expose the SOAP service to receive notifications
         */
		try {
        	this.wsnListener = wsnListener;
        	this.wsnListener.setDcepNode(this.dEtalis);
            
            QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumer");
            QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumerService");
            QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumerPort");
 
            final String SOAP_URI_LOCAL = constants.getProperty("dcep.notify.endpoint.local");
            logger.info("Exposing SOAP notification endpoint at: {} which should be reachable at {}.", SOAP_URI_LOCAL, SOAP_URI);
            NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                    serviceName, endpointName, "NotificationConsumerService.wsdl", SOAP_URI_LOCAL,
                    this.wsnListener);
            Exposer exposer = new CXFExposer();
            notifyReceiverSoap = exposer.expose(service);
            notifyReceiverSoap.start();
		} catch (Exception e) {
			throw new EcConnectionmanagerException("Error while starting DSB listener (SOAP service).", e);
		}
		
        /*
         * Expose the REST service to receive notifications
         */
		try {
        	this.restListener = restListener;
        	this.restListener.setDcepNode(this.dEtalis);

    		final ResourceConfig rc = new ResourceConfig()
    				.register(MoxyJsonFeature.class)
    				.register(restListener);

    		final String REST_URI_LOCAL = constants.getProperty("dcep.notify.rest.local");
            logger.info("Exposing REST notification endpoint at: {} which should be reachable at {}.", REST_URI_LOCAL, REST_URI);
    		notifyReceiverRest = new Server(URI.create(REST_URI_LOCAL).getPort());
            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath("/");
            ServletHolder h = new ServletHolder(new ServletContainer(rc));
            context.addServlet(h, "/");
            notifyReceiverRest.setHandler(context);
            notifyReceiverRest.start();
		} catch (Exception e) {
			throw new EcConnectionmanagerException("Error while starting DSB listener (REST service).", e);
		}
		
		/*
		 * Check for existence of topics but only fail if the service has problems
		 */
		try {
			final List<String> topics = this.rdfReceiver.getTopics();
			if (topics.isEmpty()) {
				logger.warn("No topics were found in DSB, possible misconfiguration of event adapters.");
			} else {
				for (String topic : topics) {
					logger.info("Topic on the DSB: {}", topic);
				}
			}
		} catch (Exception e) {
			throw new EcConnectionmanagerException("Error while checking the DSB.", e);
		}
		
		/*
		 * Clean up left-over subscriptions from possible previous crash
		 */
		try {
			persistence = new Sqlite();
			for (SubscriptionPerCloud sub : persistence.getSubscriptions()) {
				logger.info("Cleaning stale subscription from cloud {}: {}", sub.cloudId,
						sub.subscriptionId);
				try {
					rdfReceiver.unsubscribe(sub.subscriptionId);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
			}
			persistence.deleteAllSubscriptions();
		} catch (PersistenceException e) {
			throw new EcConnectionmanagerException(e.getMessage(), e);
		}
		
		init = true;
	}
	
	@Override
	public void destroy() {
		logger.info("Terminating {}.", this.getClass().getSimpleName());
		logger.info("Unsubscribe from Topics");
	
		// Unsubscribe
		this.rdfReceiver.unsubscribeAll();
		subscriptions.clear();
		persistence.deleteAllSubscriptions();
		
    	if (this.notifyReceiverSoap != null) {
    		this.notifyReceiverSoap.stop();
    	}

    	if (this.notifyReceiverRest != null) {
    		try {
				this.notifyReceiverRest.stop();
			} catch (Exception e) {
				logger.error("Exception while stoppping REST server. Nothing we can do now. {}", e.getMessage());
			}
    		this.notifyReceiverRest.destroy();
    	}
     	
  		init = false;
	}

	/**
	 * Persist data in historic storage.
	 * 
	 * @param event event containing quadruples
	 * @param cloudId the cloud ID to allow partitioning of storage
	 */
	@Override
	public abstract void putDataInCloud(EventType event, String cloudId);

	/**
	 * Retreive data from historic storage using a SPARQL SELECT query. SPARQL 1.1
	 * enhancements like the VALUES clause are allowed.
	 */
	@Override
	public abstract SelectResults getDataFromCloud(String query, String cloudId)
			throws EcConnectionmanagerException;

	@Override
	public abstract void publish(EventType event);

	@Override
	public void registerEventPattern(BdplQuery bdplQuery) throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		for (String cloudId : bdplQuery.getDetails().getInputStreams()) {
			subscribe(cloudId);
		}

		// Nothing to do for output streams, they are stateless
	}

	@Override
	public void unregisterEventPattern(BdplQuery bdplQuery) {
		for (String cloudId : bdplQuery.getDetails().getInputStreams()) {
			SubscriptionUsage sub = this.subscriptions.get(cloudId);
			if (sub != null) {
				unsubscribe(cloudId, sub.sub);
			}
		}
	}

	/**
	 * Subscribe to a given topic on the DSB. Duplicate subscriptions are handled using counters.
	 */
	private void subscribe(String cloudId) throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}

		if (this.subscriptions.containsKey(cloudId)) {
			logger.info("Still subscribed to topic {}.", cloudId);
			this.subscriptions.get(cloudId).usage++;
		}
		else {
			logger.info("Subscribing to topic {}.", cloudId);
			String subId = this.rdfReceiver.subscribe(cloudId, SOAP_URI);
			this.subscriptions.put(cloudId, new SubscriptionUsage(subId));
			this.persistence.storeSubscription(cloudId, subId);
		}
	}

	/**
	 * Unsubscribe from a given topic on the DSB. Duplicate subscriptions are handled using counters.
	 */
	private void unsubscribe(String cloudId, String subId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		if (this.subscriptions.containsKey(cloudId)) {
			this.subscriptions.get(cloudId).usage--;
			
			if (this.subscriptions.get(cloudId).usage == 0) {
				logger.info("Unsubscribing from topic {}.", cloudId);
				rdfReceiver.unsubscribe(subId);
				this.subscriptions.remove(cloudId);
			}
			else {
				logger.info("Still subscribed to topic {}.", cloudId);
			}
		}
	}
	
	/**
	 * Usage counter for a subscription.
	 */
	private class SubscriptionUsage implements Serializable {
		
		private static final long serialVersionUID = 100L;
		
		public SubscriptionUsage(String sub) {
			this.sub = sub;
			this.usage = 1;
		}
		
		public String sub;
		public int usage;
	}

	public AbstractReceiverRest getRdfReceiver() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}

		return this.rdfReceiver;
	}

	public AbstractSenderRest getRdfSender() {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}

		return this.rdfSender;
	}

}
