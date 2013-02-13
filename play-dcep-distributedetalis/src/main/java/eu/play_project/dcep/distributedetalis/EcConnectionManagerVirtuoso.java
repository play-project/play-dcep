package eu.play_project.dcep.distributedetalis;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.xml.namespace.QName;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoDataSource;

import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_eventadapter.AbstractReceiver;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;

public class EcConnectionManagerVirtuoso extends EcConnectionManagerNet {
	private Map<String, PublishApi> outputClouds;
	private Map<String, SubscribeApi> inputClouds;
	private Map<SubscribeApi, SubscriptionUsage> subscriptions = new HashMap<SubscribeApi, SubscriptionUsage>();
	private VirtuosoDataSource ds;
	private Logger logger = LoggerFactory.getLogger(EcConnectionManagerVirtuoso.class);
	private static final long serialVersionUID = 1L;
	private INotificationConsumer dsbListener;
	private boolean init = false;
	private AbstractReceiver rdfReceiver;
	public static String notificationReceiverEndpoint = "http://localhost:9998/foo/bar/NotificationConsumerService";


	public EcConnectionManagerVirtuoso() throws NamingException, DistributedEtalisException {
		Properties constants = Constants.getProperties("play-dcep-distribution.properties");

		ds = new VirtuosoDataSource();
		ds.setServerName(constants.getProperty("dcep.virtuoso.servername"));
		ds.setPortNumber(Integer.parseInt(constants.getProperty("dcep.virtuoso.port")));
		ds.setUser(constants.getProperty("dcep.virtuoso.user"));
		ds.setPassword(constants.getProperty("dcep.virtuoso.password"));
		init();
	}
	
	public EcConnectionManagerVirtuoso(String server, int port, String user, String pw) throws DistributedEtalisException {
		ds = new VirtuosoDataSource();
		ds.setServerName(server);
		ds.setPortNumber(port);
		ds.setUser(user);
		ds.setPassword(pw);
		init();
	}
	
	private void init() throws DistributedEtalisException {
		this.rdfReceiver = new AbstractReceiver() {};
		
        // instanciate the WSN server stuff...
        Service server = null;

        try {
        	this.dsbListener = new EcConnectionListenerVirtuoso(this.rdfReceiver);
            
            QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumer");
            QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumerService");
            QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumerPort");
            // expose the service
            NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                    serviceName, endpointName, "NotificationConsumerService.wsdl", notificationReceiverEndpoint,
                    this.dsbListener);
            Exposer exposer = new CXFExposer();
            server = exposer.expose(service);
            server.start();

        } catch (Exception e) {
            throw new DistributedEtalisException("Error while starting DSB listener.", e);
        }
        
		init = true;
	}
	
	@Override
	public synchronized SelectResults getDataFromCloud(String query, String cloudId) 
			throws EcConnectionmanagerException, MalformedSparqlQueryException
			{
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		List<String> variables = new ArrayList<String>();
		List<List> result = new ArrayList<List>();
		
		Connection con = null;
		try {
			con = ds.getConnection();
			Statement sta = con.createStatement();
			ResultSet res = sta.executeQuery("sparql "+query);
			
			ResultSetMetaData rmd = res.getMetaData();
			int colNum = rmd.getColumnCount();
			for(int i = 0; i < colNum; i++){
				variables.add(rmd.getColumnName(i));
			}
			
			//TODO result create, select variable analyze, create
			while(res.next()){
				ArrayList<String> data = new ArrayList<String>();
				for(int i = 0; i < colNum; i++)
					data.add(res.getString(i));
				result.add(data);
			}
			
		} catch (SQLException e) {
			logger.error("Exception with Virtuoso", e);
			e.printStackTrace();
		} finally {
			if(con != null)
				try {
					con.close();
				} catch (SQLException e) {
					logger.error("Connection Exception with Virtuoso", e);
					e.printStackTrace();
				}
		}
		
		ResultRegistry rr = new ResultRegistry();
		rr.setResult(result);
		rr.setVariables(variables);
		return rr;
	}

	@Override
	public PutGetApi getHistoricCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		// TODO Auto-generated method stub
		return super.getHistoricCloud(cloudId);
	}

	@Override
	public SubscribeApi getInputCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		// TODO Auto-generated method stub
		return super.getInputCloud(cloudId);
	}

	@Override
	public PublishApi getOutputCloud(String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		// TODO Auto-generated method stub
		return super.getOutputCloud(cloudId);
	}

	@Override
	public void publish(CompoundEvent event) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		// TODO Auto-generated method stub
		super.publish(event);
	}

	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		for (String cloudId : epSparqlQuery.getQueryDetails().getInputStreams()) {
			subscribe(cloudId);
		}

		// Nothing to do for output streams, they are stateless
	}

	@Override
	public void unregisterEventPattern(EpSparqlQuery epSparqlQuery) {
		// TODO Auto-generated method stub
		super.unregisterEventPattern(epSparqlQuery);
	}

	@Override
	public void subscribe(String cloudId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}

		try {
			if (this.subscriptions.containsKey(getInputCloud(cloudId))) {
				logger.info("Still subscribed to eventcloud {}.", cloudId);
				this.subscriptions.get(getInputCloud(cloudId)).usage++;
			}
			else {
				logger.info("Subscribing to eventcloud {}.", cloudId);
				//this.getInputCloud(cloudId).subscribe(sub, eventCloudListener);

				int index = cloudId.lastIndexOf("/");
				QName topic = new QName(cloudId.substring(0, index), cloudId.substring(index + 1));
				
				this.rdfReceiver.subscribe(topic, notificationReceiverEndpoint);
				//this.subscriptions.put(getInputCloud(cloudId), new SubscriptionUsage(sub));
			}
		} catch (NotificationException e) {
			logger.error("Problem subscribing to topic {}: {}", cloudId, e.getMessage());
		} catch (EcConnectionmanagerException e) {
			logger.error("Problem subscribing to topic {}: {}", cloudId, e.getMessage());
		}
	}

	@Override
	public void unsubscribe(String cloudId, Subscription sub) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		try {
			if (this.subscriptions.containsKey(getInputCloud(cloudId))) {
				this.subscriptions.get(getInputCloud(cloudId)).usage--;
				
				if (this.subscriptions.get(getInputCloud(cloudId)).usage == 0) {
					logger.info("Unsubscribing from eventcloud {}.", cloudId);
					getInputCloud(cloudId).unsubscribe(sub.getId());
					this.subscriptions.remove(getInputCloud(cloudId));
					this.inputClouds.remove(cloudId);
				}
				else {
					logger.info("Still subscribed to eventcloud {}.", cloudId);
				}
			}
		} catch (EcConnectionmanagerException e) {
			logger.error("Problem unsubscribing from topic {}: {}", cloudId, e.getMessage());
		}
	}
	
	/**
	 * Usage counter for a subscription.
	 */
	private class SubscriptionUsage implements Serializable {
		
		private static final long serialVersionUID = -6063251924935507681L;
		
		public SubscriptionUsage(Subscription sub) {
			this.sub = sub;
			this.usage = 1;
		}
		
		public Subscription sub;
		public int usage;
	}
}
