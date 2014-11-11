package eu.play_project.dcep.node.persistence;

import java.util.Set;

import eu.play_project.dcep.node.persistence.Sqlite.SubscriptionPerCloud;

public interface Persistence {

	/**
	 * Store subscriptions to recover from any future crashes.
	 */
	public void storeSubscription(String cloudId, String subscriptionId);

	/**
	 * Empty the store of recovery-subscriptions.
	 */
	public void deleteAllSubscriptions();

	/**
	 * Load stale subscriptions to clean up from any previous crashes.
	 */
	public Set<SubscriptionPerCloud> getSubscriptions() throws PersistenceException;

}
