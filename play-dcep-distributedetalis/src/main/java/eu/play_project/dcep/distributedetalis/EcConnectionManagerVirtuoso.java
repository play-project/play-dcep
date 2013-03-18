package eu.play_project.dcep.distributedetalis;

import java.io.ByteArrayOutputStream;
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

import javax.xml.namespace.QName;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import virtuoso.jdbc3.VirtuosoDataSource;
import virtuoso.jena.driver.VirtGraph;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventformat.EventFormatHelpers;
import eu.play_project.play_eventadapter.AbstractReceiver;
import eu.play_project.play_eventadapter.AbstractSender;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.utils.trigwriter.TriGWriter;

public class EcConnectionManagerVirtuoso implements EcConnectionManager {
	private final Map<String, SubscriptionUsage> subscriptions = new HashMap<String, SubscriptionUsage>();
	private final VirtuosoDataSource virtuoso;
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerVirtuoso.class);
	private boolean init = false;
	private AbstractReceiver rdfReceiver;
	private AbstractSender rdfSender;
	private final DistributedEtalis dEtalis;
	private EcConnectionListenerVirtuoso dsbListener;
	static final Properties constants = DcepConstants.getProperties();
	public static String notificationReceiverEndpoint = constants.getProperty("dcep.notify.endpoint");
	private Service notifyReceiverServer;


	public EcConnectionManagerVirtuoso(DistributedEtalis dEtalis) throws DistributedEtalisException {
		this(
				constants.getProperty("dcep.virtuoso.servername"),
				Integer.parseInt(constants.getProperty("dcep.virtuoso.port")),
				constants.getProperty("dcep.virtuoso.user"),
				constants.getProperty("dcep.virtuoso.password"),
				dEtalis
				);
	}
	
	public EcConnectionManagerVirtuoso(String server, int port, String user, String pw, DistributedEtalis dEtalis) throws DistributedEtalisException {
		virtuoso = new VirtuosoDataSource();
		virtuoso.setServerName(server);
		virtuoso.setPortNumber(port);
		virtuoso.setUser(user);
		virtuoso.setPassword(pw);
		this.dEtalis = dEtalis;

		// Test Virtuoso JDBC connection
		try {
			virtuoso.getConnection().close();
		} catch (SQLException e) {
			throw new DistributedEtalisException("Could not connect to Virtuoso.", e);
		}

		init();
	}
	
	private void init() throws DistributedEtalisException {
		if (init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has ALREADY been initialized.");
		}

		logger.info("Initialising {}.", this.getClass().getSimpleName());
		
		this.rdfReceiver = new AbstractReceiver(
				constants.getProperty("dsb.subscribe.endpoint"),
				constants.getProperty("dsb.unsubscribe.endpoint")) {};
				
		// Use an arbitrary topic as default:
		this.rdfSender = new AbstractSender(Stream.FacebookCepResults.getTopicQName()) {};
		this.rdfSender.setDsbNotify(constants.getProperty(
				"dsb.notify.endpoint"));

		try {
        	this.dsbListener = new EcConnectionListenerVirtuoso(this.rdfReceiver);
        	this.dsbListener.setDetalis(this.dEtalis);
            
            QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumer");
            QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumerService");
            QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                    "NotificationConsumerPort");
            // expose the service
            final String notificationReceiverEndpointLocal = constants.getProperty("dcep.notify.endpoint.local");
            logger.info("Exposing notification endpoint at: {} which should be reachable at {}.", notificationReceiverEndpointLocal, notificationReceiverEndpoint);
            NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                    serviceName, endpointName, "NotificationConsumerService.wsdl", notificationReceiverEndpointLocal,
                    this.dsbListener);
            Exposer exposer = new CXFExposer();
            notifyReceiverServer = exposer.expose(service);
            notifyReceiverServer.start();

        } catch (Exception e) {
        	
        	if (notifyReceiverServer != null) {
        		notifyReceiverServer.stop();
        	}
            throw new DistributedEtalisException("Error while starting DSB listener.", e);
        }
        
		init = true;
	}
	
	@Override
	public void destroy() {
		logger.info("Terminating {}.", this.getClass()
				.getSimpleName());
		logger.info("Unsubscribe from Topics");
	
		// Unsubscribe
		this.rdfReceiver.unsubscribeAll();
		subscriptions.clear();
		
    	if (this.notifyReceiverServer != null) {
    		this.notifyReceiverServer.stop();
    	}
    	
  		init = false;
	}

	public void putDataInCloud(CompoundEvent event, String cloudId) {

		VirtGraph graph = new VirtGraph(event.getGraph().toString(), this.virtuoso);
		for (Quadruple e : event) {
			graph.add(new Triple(e.getSubject(), e.getPredicate(), e.getObject()));
		}
		graph.close();
	}

	@Override
	public synchronized SelectResults getDataFromCloud(String query, String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		List<String> variables = new ArrayList<String>();
		List<List> result = new ArrayList<List>();
		
		Connection con = null;
		try {
			con = virtuoso.getConnection();
			Statement sta = con.createStatement();
			logger.debug("Sending historical query to Virtuoso: \n" + query);
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
		} finally {
			if(con != null)
				try {
					con.close();
				} catch (SQLException e) {
					logger.error("Connection Exception with Virtuoso", e);
				}
		}
		
		ResultRegistry rr = new ResultRegistry();
		rr.setResult(result);
		rr.setVariables(variables);
		return rr;
	}

	private QName getTopic(String cloudId) {
		int index = cloudId.lastIndexOf("/");
		return new QName(cloudId.substring(0, index+1), cloudId.substring(index + 1), "s");
	}

	@Override
	public void publish(CompoundEvent event) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		String cloudId = EventCloudHelpers.getCloudId(event);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

		TriGWriter.write(out, DatasetFactory.create(quadruplesToDatasetGraph(event.getQuadruples())));

		Element notifPayload = EventFormatHelpers.wrapWithDomNativeMessageElement(new String(out.toByteArray()));
		
		this.rdfSender.notify(notifPayload, getTopic(cloudId));
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
		for (String cloudId : epSparqlQuery.getQueryDetails().getInputStreams()) {
			unsubscribe(cloudId, this.subscriptions.get(cloudId).sub);
		}
	}

	private void subscribe(String cloudId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}

		try {
			if (this.subscriptions.containsKey(cloudId)) {
				logger.info("Still subscribed to topic {}.", cloudId);
				this.subscriptions.get(cloudId).usage++;
			}
			else {
				logger.info("Subscribing to topic {}.", cloudId);
				QName topic = getTopic(cloudId);
				String subId = this.rdfReceiver.subscribe(topic, notificationReceiverEndpoint);
				this.subscriptions.put(cloudId, new SubscriptionUsage(subId));

			}
		} catch (NotificationException e) {
			logger.error("Problem subscribing to topic {}: {}", cloudId, e.getMessage());
		}
	}

	private void unsubscribe(String cloudId, String subId) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		try {
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
		} catch (NotificationException e) {
			logger.error("Problem unsubscribing from topic {}: {}", cloudId, e.getMessage());
		}
	}
	
	/**
	 * Usage counter for a subscription.
	 */
	private class SubscriptionUsage implements Serializable {
		
		private static final long serialVersionUID = -6063251924935507681L;
		
		public SubscriptionUsage(String sub) {
			this.sub = sub;
			this.usage = 1;
		}
		
		public String sub;
		public int usage;
	}

    /**
     * A private method to convert a collection of quadruples into the
     * corresponding data set graph to be used in the event format writers
     * 
     * @author ialshaba
     * 
     * @param quads
     *            the collection of the quadruples
     * @return the corresponding data set graph
     */
    private static DatasetGraph quadruplesToDatasetGraph(List<Quadruple> quads) {
        DatasetGraph dsg = DatasetGraphFactory.createMem();
        for (Quadruple q : quads) {
            if (q.getPredicate() != PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE) {
                dsg.add(
                        q.getGraph(), q.getSubject(), q.getPredicate(),
                        q.getObject());
            }
        }

        return dsg;
    }

}
