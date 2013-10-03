package eu.play_project.dcep.distributedetalis;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerNet;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
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
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;

public class EcConnectionManagerNet implements SimplePublishApi, Serializable,
		EcConnectionManager {

	private static final long serialVersionUID = 100L;

	private String eventCloudRegistryUrl;

	private Map<String, PublishApi> outputClouds;
	private Map<String, SubscribeApi> inputClouds;
	private Map<String, PutGetApi> putGetClouds;
	private final Map<SubscribeApi, SubscriptionUsage> subscriptions = new HashMap<SubscribeApi, SubscriptionUsage>();
	public static LinkedList<CompoundEvent> eventInputQueue;
	private EcConnectionListenerNet eventCloudListener;
	static GetEventThread getEventThread;
	private boolean init = false;
	private Logger logger;
	

	public EcConnectionManagerNet() {}

	public EcConnectionManagerNet(String eventCloudRegistry,
			DistributedEtalis dEtalis) throws EcConnectionmanagerException {
		
		logger = LoggerFactory.getLogger(EcConnectionManagerNet.class);
		logger.info("Initialising {}.", this.getClass().getSimpleName());

		putGetClouds = new HashMap<String, PutGetApi>();
		outputClouds = new HashMap<String, PublishApi>();
		inputClouds = new HashMap<String, SubscribeApi>();
		eventCloudRegistryUrl = eventCloudRegistry;
		eventInputQueue = new LinkedList<CompoundEvent>();
		eventCloudListener = new EcConnectionListenerNet();
		getEventThread = new GetEventThread(dEtalis); // Publish events from queue to dEtalis.
		new Thread(getEventThread).start();
		
		try {
			final Set<EventCloudId> cloudIds = EventCloudsRegistryFactory.lookupEventCloudsRegistry(eventCloudRegistryUrl).listEventClouds();
			if (cloudIds.isEmpty()) {
				logger.warn("No cloudIds were found in EventCloud, possible misconfiguration.");
			} else {
				for (EventCloudId cloudId : cloudIds) {
					logger.info("CloudId in EventCloud: " + cloudId);
				}
			}
		} catch (IOException e) {
			throw new EcConnectionmanagerException(String.format("Error probing EventCloud registry at: '%s': %s", eventCloudRegistryUrl, e.getMessage()));
		}
		
		this.init = true;
	}

	@Override
	public synchronized SelectResults getDataFromCloud(String query,
			String cloudId) throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		logger.info("Get data from EventCloud '" + cloudId + "' with query : "
				+ query);

		PutGetApi putGetCloud;
		SparqlSelectResponse response = null;
		try {
			putGetCloud = getHistoricCloud(cloudId);
			response = putGetCloud.executeSparqlSelect(query);
		} catch (EcConnectionmanagerException e) {
			logger.error("Error while connecting to event cloud {}.", cloudId);
			throw e;
		} catch (MalformedSparqlQueryException e) {
			logger.error("Malformed sparql query. " + e.getMessage());
			throw new EcConnectionmanagerException(e.getMessage(), e);
		}
		ResultSetWrapper rw = response.getResult();
		return new ResultRegistry(rw);
	}
	
	/**
	 * Persist data in historic storage.
	 * 
	 * @param event event containing quadruples
	 * @param cloudId the cloud ID to allow partitioning of storage
	 */
	@Override
	public void putDataInCloud(CompoundEvent event, String cloudId) throws EcConnectionmanagerException {
		PutGetApi putGetApi = getHistoricCloud(cloudId);
		for (Quadruple quad : event) {
			putGetApi.add(quad);
		}
	}


	private PutGetApi getHistoricCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		try {
			if (!putGetClouds.containsKey(cloudId)) {
				PutGetApi proxy = ProxyFactory.newPutGetProxy(
						eventCloudRegistryUrl,
						new EventCloudId(
								eu.play_project.play_commons.constants.Stream
										.toTopicUri(cloudId)));
				putGetClouds.put(cloudId, proxy);
			}
		} catch (EventCloudIdNotManaged e) {
			throw new EcConnectionmanagerException(e.getMessage(), e);
		}
		return putGetClouds.get(cloudId);
	}

	private SubscribeApi getInputCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		try {
			if (!inputClouds.containsKey(cloudId)) {
				SubscribeApi proxy = ProxyFactory.newSubscribeProxy(
						eventCloudRegistryUrl, new EventCloudId(cloudId));
				inputClouds.put(cloudId, proxy);
			}
		} catch (EventCloudIdNotManaged e) {
			throw new EcConnectionmanagerException(e.getMessage(), e);
		}
		return inputClouds.get(cloudId);
	}

	private PublishApi getOutputCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " has not been initialized.");
		}

		try {
			if (!outputClouds.containsKey(cloudId)) {
				PublishApi proxy = ProxyFactory.newPublishProxy(
						eventCloudRegistryUrl, new EventCloudId(cloudId));
				outputClouds.put(cloudId, proxy);
			}
		} catch (EventCloudIdNotManaged e) {
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

		try {
			this.getOutputCloud(cloudId).publish(event);
		} catch (EcConnectionmanagerException e) {
			logger.error("Event could not be published to cloud {}.", cloudId);
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
		for (String cloudId : bdplQuery.getDetails().getInputStreams()) {
			try {
				unsubscribe(cloudId,
						this.subscriptions.get(getInputCloud(cloudId)).sub);
			} catch (EcConnectionmanagerException e) {
				logger.error("Incurred unknown event cloud {}.", cloudId);
			}
		}

		// TODO stuehmer: handle (i.e. close) other proxy connections, too?
		// needs counters!
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
				this.subscriptions.put(getInputCloud(cloudId),
						new SubscriptionUsage(sub));
			}
		} catch (EcConnectionmanagerException e) {
			logger.error("Problem subscribing to event cloud {}: {}", cloudId,
					e.getMessage());
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
			logger.error("Problem unsubscribing from event cloud {}: {}",
					cloudId, e.getMessage());
		}
	}

	@Override
	public void destroy() {
		if(logger==null){
			logger = LoggerFactory.getLogger(EcConnectionManagerNet.class);
		}
		logger.info("Terminating {}.", this.getClass().getSimpleName());
		logger.info("Unsubscribe from Event Clouds");

		// Unsubscribe
		for (SubscribeApi proxy : subscriptions.keySet()) {
			proxy.unsubscribe(subscriptions.get(proxy).sub.getId());
		}
		
		if(getEventThread!=null) getEventThread.stop();
		if(subscriptions!=null) subscriptions.clear();
		if(inputClouds!=null) inputClouds.clear();
		if(outputClouds!=null) outputClouds.clear();

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
				synchronized (eventInputQueue) {
					while (this.getEventThread == Thread.currentThread()) {
						if (!eventInputQueue.isEmpty()) {
							dEtalis.publish(eventInputQueue.poll());
						} else {
							try {
								eventInputQueue.wait();
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
					}
				}
			}
		}
		
		/*
		 * See <http://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html>
		 */
	    public void stop() {
	        Thread stopMe = getEventThread;
	        getEventThread = null;
	        stopMe.interrupt();
	    }

	}
	
	public class EcConnectionListenerNet2 extends CompoundEventNotificationListener implements Serializable {
		private static final long serialVersionUID = 100L;

		// For ProActive:
		public EcConnectionListenerNet2(){}

		@Override
		public void onNotification(SubscriptionId id, CompoundEvent event) {
			synchronized(eventInputQueue){
				eventInputQueue.add(event);
				eventInputQueue.notifyAll();
			}
		}
	}
}
