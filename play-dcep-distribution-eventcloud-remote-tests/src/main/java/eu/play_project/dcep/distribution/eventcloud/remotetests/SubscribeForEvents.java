package eu.play_project.dcep.distribution.eventcloud.remotetests;


import eu.play_project.play_commons.constants.Stream;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;

public class SubscribeForEvents {
	Subscription subscription;
	SubscribeApi subscribeProxy;

	public void subscribe(String registry, Stream streamId, String query) throws EventCloudIdNotManaged{
		
		subscribeProxy = ProxyFactory.newSubscribeProxy(registry, new EventCloudId(streamId.getTopicUri()));

		System.out.println("Subscribe to EventCloud with id " + streamId.getTopicUri());

		subscription = new Subscription(query);
		subscribeProxy.subscribe(subscription, new PrintEventNotificationListener());
	}
	
	public void unsubscribe(){
		// Unsubscribe
		subscribeProxy.unsubscribe(subscription.getId());
	}
}
