package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_EXIT;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_EXIT;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.objectweb.proactive.api.PAFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerNet;
import eu.play_project.dcep.distributedetalis.persistence.Persistence;
import eu.play_project.dcep.distributedetalis.persistence.PersistenceException;
import eu.play_project.dcep.distributedetalis.persistence.Sqlite;
import eu.play_project.dcep.distributedetalis.persistence.Sqlite.SubscriptionPerCloud;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Event;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_platformservices.api.BdplQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;

/**
 * The connection manager to get data from and send data to <a
 * href="http://eventcloud.inria.fr/">EventCloud</a> (<a
 * href="https://bitbucket.org/eventcloud/eventcloud">Project Page</a>).
 * 
 * EventCloud is a storage and publish/subscribe component to handle (e.g.
 * store) events in streams. A stream is called <i>cloud</i> so EventCloud
 * manages many <i>clouds</i> identified by their {@code cloudId}.
 * 
 * A <i>cloud</i> (e.g. the one to store {@link Stream#TwitterFeed}) can have
 * three kinds of proxies depending on how the EventCloud must be used, i.e. to
 * publish complex events or to subscribe to simple events or to get historic
 * data.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 */
public class EcConnectionManagerNet implements Serializable, EcConnectionManager {

	private static final long serialVersionUID = 100L;

	private String eventCloudRegistryUrl;

	/** For each cloudId store a resuable publish proxy to EventCloud, i.e., 1:1 mapping */
	private final Map<String, PublishApi> outputClouds = new HashMap<String, PublishApi>();
	/** For each cloudId store a resuable subscribe proxy to EventCloud, i.e., 1:1 mapping */
	private final Map<String, SubscribeApi> inputClouds = new HashMap<String, SubscribeApi>();
	/** For each cloudId store a pool of resuable publish proxies to EventCloud, i.e., 1:n mapping */
	private GenericKeyedObjectPool<String, PutGetApi> historicCloudsPool;
	
	private final Map<SubscribeApi, SubscriptionUsage> subscriptions = new HashMap<SubscribeApi, SubscriptionUsage>();
	private final BlockingQueue<CompoundEvent> eventInputQueue = new LinkedBlockingQueue<CompoundEvent>();
	private EcConnectionListenerNet eventCloudListener;
	private GetEventThread getEventThread;
	private boolean init = false;
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerNet.class);;
	private Persistence persistence;
	static final Properties constants = DcepConstants.getProperties();
	public static final String REST_URI = constants.getProperty("dcep.notify.rest.local");

	public EcConnectionManagerNet() {
	}

	public EcConnectionManagerNet(String eventCloudRegistry, DistributedEtalis dEtalis)
			throws EcConnectionmanagerException {

		logger.info("Initialising {}.", this.getClass().getSimpleName());
		
		/*
		 * Management of EventCloud and its proxies
		 */
		GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
		config.setJmxEnabled(false);
		// Number of proxies per cloudId
		config.setMaxTotalPerKey(Integer.parseInt(constants.getProperty("dcep.eventcloud.putgetproxies.per.cloud")));
		historicCloudsPool = new GenericKeyedObjectPool<String, PutGetApi>(new PutGetProxyPoolFactory(), config);
		eventCloudRegistryUrl = eventCloudRegistry;
		eventCloudListener = new EcConnectionListenerNet(eventInputQueue);
		
		getEventThread = new GetEventThread(dEtalis); // Publish events from queue to dEtalis.
		new Thread(getEventThread).start();

		try {
			final Set<EventCloudId> cloudIds = EventCloudsRegistryFactory
					.lookupEventCloudsRegistry(eventCloudRegistryUrl).listEventClouds();
			if (cloudIds.isEmpty()) {
				logger.warn("No cloudIds were found in EventCloud, possible misconfiguration.");
			} else {
				for (EventCloudId cloudId : cloudIds) {
					logger.info("CloudId in EventCloud: {}", cloudId);
				}
			}
		} catch (IOException e) {
			throw new EcConnectionmanagerException(String.format(
					"Error probing EventCloud registry at: '%s': %s", eventCloudRegistryUrl,
					e.getMessage()));
		}

		try {
			persistence = new Sqlite();
			for (SubscriptionPerCloud sub : persistence.getSubscriptions()) {
				logger.info("Cleaning stale subscription from cloud {}: {}", sub.cloudId,
						sub.subscriptionId);

				try {
					ProxyFactory.newSubscribeProxy(eventCloudRegistryUrl,
							new EventCloudId(sub.cloudId)).unsubscribe(SubscriptionId.parseSubscriptionId(sub.subscriptionId));
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
			}

		} catch (PersistenceException e) {
			throw new EcConnectionmanagerException(e.getMessage(), e);
		}

		this.init = true;
	}

	@Override
	public SelectResults getDataFromCloud(String query, String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		logger.debug("Get data from EventCloud '{}' with query:\n{}", cloudId, query);

		PutGetApi putGetCloud = null;
		SparqlSelectResponse response = null;
		try {
			putGetCloud = historicCloudsPool.borrowObject(cloudId);
			if (logger.isDebugEnabled()) {
				long beginQuerying = System.currentTimeMillis();
				response = PAFuture.getFutureValue(putGetCloud.executeSparqlSelect(query));
				logger.debug("Get data from EventCloud '{}' had latency {} ms", cloudId, System.currentTimeMillis() - beginQuerying);
				logger.debug("Get data from EventCloud '{}' had statistics {}", cloudId, response.getStats());
			} else {
				response = putGetCloud.executeSparqlSelect(query);
			}
		} catch (EcConnectionmanagerException e) {
			logger.error("Error while connecting to event cloud {}.", cloudId);
			throw e;
		} catch (MalformedSparqlQueryException e) {
			logger.error("Malformed sparql query. {}", e.getMessage());
			throw new EcConnectionmanagerException(e.getMessage(), e);
		} catch (Exception e) {
			logger.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
			throw new EcConnectionmanagerException(e.getMessage(), e);
		} finally {
			if (putGetCloud != null) {
				historicCloudsPool.returnObject(cloudId, putGetCloud);
			}
		}
		ResultSetWrapper rw = response.getResult();
		return ResultRegistry.makeResult(rw);
	}

	/**
	 * Persist data in historic storage.
	 * 
	 * @param event
	 *            event containing quadruples
	 * @param cloudId
	 *            the cloud ID to allow partitioning of storage
	 */
	@Override
	public void putDataInCloud(CompoundEvent event, String cloudId)
			throws EcConnectionmanagerException {
		PutGetApi putGetApi = null;
		try {
			putGetApi = historicCloudsPool.borrowObject(cloudId);
			for (Quadruple quad : event) {
				putGetApi.add(quad);
			}
		} catch (EcConnectionmanagerException e) {
			logger.error("Error while connecting to event cloud {}.", cloudId);
			throw e;
		} catch (Exception e) {
			logger.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
			throw new EcConnectionmanagerException(e.getMessage(), e);
		} finally {
			if (putGetApi != null) {
				historicCloudsPool.returnObject(cloudId, putGetApi);
			}
		}
	}

	private synchronized SubscribeApi getInputCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		try {
			if (!inputClouds.containsKey(cloudId)) {
				SubscribeApi proxy = ProxyFactory.newSubscribeProxy(eventCloudRegistryUrl,
						new EventCloudId(cloudId));
				inputClouds.put(cloudId, proxy);
			}
		} catch (EventCloudIdNotManaged e) {
			throw new EcConnectionmanagerException(e.getMessage(), e);
		} catch (Exception e) { // we get various runtime exceptions here
			throw new EcConnectionmanagerException(e.getMessage(), e);
		}
		return inputClouds.get(cloudId);
	}

	private synchronized PublishApi getOutputCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		try {
			if (!outputClouds.containsKey(cloudId)) {
				PublishApi proxy = ProxyFactory.newPublishProxy(eventCloudRegistryUrl,
						new EventCloudId(cloudId));
				outputClouds.put(cloudId, proxy);
			}
		} catch (EventCloudIdNotManaged e) {
			throw new EcConnectionmanagerException(e.getMessage(), e);
		} catch (Exception e) { // we get various runtime exceptions here
			throw new EcConnectionmanagerException(e.getMessage(), e);
		}
		return outputClouds.get(cloudId);
	}

	@Override
	public void publish(CompoundEvent event) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		String cloudId = EventCloudHelpers.getCloudId(event);

		if (!cloudId.isEmpty()) {
			try {
				// Do not remove this line, needed for logs. :stuehmer
				logger.info(LOG_DCEP_EXIT + event.getGraph() + " " + EventCloudHelpers.getMembers(event));
				if (logger.isDebugEnabled()) {
					logger.debug(LOG_DCEP + "Complex Event:\n{}", event.toString());
				}

				this.getOutputCloud(cloudId).publish(event);
			} catch (EcConnectionmanagerException e) {
				logger.error(LOG_DCEP_FAILED_EXIT + "Event could not be published to cloud '{}'.", cloudId);
			}
		}
		else {
			logger.warn(LOG_DCEP_FAILED_EXIT + "Got empty cloud ID from event '{}', don't know which cloud to publish to. Discarding complex event.", event.getGraph() + Event.EVENT_ID_SUFFIX);
		}
	}

	@Override
	public void registerEventPattern(BdplQuery bdplQuery) throws EcConnectionmanagerException {
		for (String cloudId : bdplQuery.getDetails().getInputStreams()) {
			subscribe(cloudId);
		}

		// Treat output streams lazily: don't connect before a complex event is
		// detected.
	}

	@Override
	public void unregisterEventPattern(BdplQuery bdplQuery) {
		// Deal with input streams
		for (String cloudId : bdplQuery.getDetails().getInputStreams()) {
			try {
				unsubscribe(cloudId, this.subscriptions.get(getInputCloud(cloudId)).sub);
			} catch (EcConnectionmanagerException e) {
				logger.error("Incurred unknown event cloud {}.", cloudId);
			}
		}
		
		// There is nothing to do for historical streams or output streams
	}

	private void subscribe(String cloudId) throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		Subscription sub = Subscription.acceptAll();

		try {
			if (this.subscriptions.containsKey(getInputCloud(cloudId))) {
				logger.info("Still subscribed to eventcloud {}.", cloudId);
				this.subscriptions.get(getInputCloud(cloudId)).usage++;
			} else {
				logger.info("Subscribing to eventcloud {}.", cloudId);
				this.getInputCloud(cloudId).subscribe(sub, eventCloudListener);
				this.subscriptions.put(getInputCloud(cloudId), new SubscriptionUsage(sub));
				this.persistence.storeSubscription(cloudId, sub.getId().toString());
			}
		} catch (EcConnectionmanagerException e) {
			logger.error("Problem subscribing to event cloud {}: {}", cloudId, e.getMessage());
			throw e;
		}
	}

	private void unsubscribe(String cloudId, Subscription sub) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		try {
			if (this.subscriptions.containsKey(getInputCloud(cloudId))) {
				this.subscriptions.get(getInputCloud(cloudId)).usage--;

				if (this.subscriptions.get(getInputCloud(cloudId)).usage == 0) {
					logger.info("Unsubscribing from eventcloud {}.", cloudId);
					getInputCloud(cloudId).unsubscribe(sub.getId());
					this.subscriptions.remove(getInputCloud(cloudId));
					this.inputClouds.remove(cloudId);
				} else {
					logger.info("Still subscribed to eventcloud {}.", cloudId);
				}
			}
		} catch (EcConnectionmanagerException e) {
			logger.error("Problem unsubscribing from event cloud {}: {}", cloudId, e.getMessage());
		}
	}

	@Override
	public void destroy() {

		logger.info("Terminating {}.", this.getClass().getSimpleName());
		logger.info("Unsubscribe from event clouds");

		// Unsubscribe
		for (SubscribeApi proxy : subscriptions.keySet()) {
			proxy.unsubscribe(subscriptions.get(proxy).sub.getId());
		}
		persistence.deleteAllSubscriptions();

		if (getEventThread != null)
			getEventThread.stop();
		if (subscriptions != null)
			subscriptions.clear();
		if (inputClouds != null)
			inputClouds.clear();
		if (outputClouds != null)
			outputClouds.clear();

		this.init = false;
	}

	/**
	 * Usage counter for a subscription.
	 */
	private class SubscriptionUsage implements Serializable {

		private static final long serialVersionUID = 100L;

		public SubscriptionUsage(Subscription sub) {
			this.sub = sub;
			this.usage = 1;
		}

		public Subscription sub;
		public int usage;
	}

	/**
	 * Take events from queue and publish them to dEtalis.
	 * 
	 * @author sobermeier
	 * 
	 */

	public class GetEventThread implements Runnable {

		private final DistributedEtalis dEtalis;
		private volatile Thread getEventThread;

		public GetEventThread(DistributedEtalis dEtalis) {
			this.dEtalis = dEtalis;
		}

		@Override
		public void run() {
			this.getEventThread = Thread.currentThread();

			while (this.getEventThread == Thread.currentThread()) {
				try {
					dEtalis.publish(eventInputQueue.take());
				} catch (InterruptedException e) {
				}
			}
		}

		/*
		 * See <http://docs.oracle.com/javase/1.5.0/docs/guide/misc/
		 * threadPrimitiveDeprecation.html>
		 */
		public void stop() {
			Thread stopMe = getEventThread;
			getEventThread = null;
			stopMe.interrupt();
		}
	}
	
	private class PutGetProxyPoolFactory extends BaseKeyedPooledObjectFactory<String, PutGetApi> {

		@Override
		public PutGetApi create(String cloudId) throws EcConnectionmanagerException {
			try {
				return ProxyFactory.newPutGetProxy(
						eventCloudRegistryUrl,
						new EventCloudId(eu.play_project.play_commons.constants.Stream
								.toTopicUri(cloudId)));
			} catch (EventCloudIdNotManaged e) {
				throw new EcConnectionmanagerException(e.getMessage(), e);
			} catch (Exception e) { // we get various runtime exceptions here
				throw new EcConnectionmanagerException(e.getMessage(), e);
			}
		}

		@Override
		public PooledObject<PutGetApi> wrap(PutGetApi proxy) {
			return new DefaultPooledObject<PutGetApi>(proxy);
		}
		
	}
}
