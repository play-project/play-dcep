package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_EXIT;
import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_EXIT;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.ontoware.rdf2go.impl.jena.TypeConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoDataSource;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;

import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerRest;
import eu.play_project.dcep.distributedetalis.listeners.EcConnectionListenerWsn;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.dcep.node.api.EcConnectionmanagerException;
import eu.play_project.dcep.node.api.SelectResults;
import eu.play_project.dcep.node.connections.AbstractConnectionManagerWsn;
import eu.play_project.play_commons.constants.Event;
import eu.play_project.play_eventadapter.AbstractReceiverRest;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;

/**
 * The connection manager to get real-time events from the PLAY Platform and get
 * historical data from the RDF store <a
 * href="http://virtuoso.openlinksw.com/">Virtuoso</a>.
 * 
 * @author Roland Stühmer
 */
public class EcConnectionManagerVirtuoso extends AbstractConnectionManagerWsn<CompoundEvent> {
	private Connection virtuosoConnection;
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerVirtuoso.class);
	
	public EcConnectionManagerVirtuoso(DistributedEtalis dEtalis) throws EcConnectionmanagerException {
		this(
				constants.getProperty("dcep.virtuoso.servername"),
				Integer.parseInt(constants.getProperty("dcep.virtuoso.port")),
				constants.getProperty("dcep.virtuoso.user"),
				constants.getProperty("dcep.virtuoso.password"),
				dEtalis
				);
	}
	
	public EcConnectionManagerVirtuoso(String server, int port, String user, String pw, DistributedEtalis dEtalis) throws EcConnectionmanagerException {
		super(dEtalis);
		
		VirtuosoDataSource virtuoso = new VirtuosoDataSource();
		virtuoso.setServerName(server);
		virtuoso.setPortNumber(port);
		virtuoso.setUser(user);
		virtuoso.setPassword(pw);

		// Test Virtuoso JDBC connection
		try {
			virtuoso.getConnection().close();
			virtuosoConnection = virtuoso.getConnection();
		} catch (SQLException e) {
			throw new EcConnectionmanagerException("Could not connect to Virtuoso.", e);
		}

		AbstractReceiverRest receiver = new AbstractReceiverRest() {};
		super.init(new EcConnectionListenerWsn(receiver), new EcConnectionListenerRest(receiver));
	}

	/**
	 * Persist data in historic storage.
	 * 
	 * @param event event containing quadruples
	 * @param cloudId the cloud ID to allow partitioning of storage
	 */
	@Override
	public void putDataInCloud(CompoundEvent event, String cloudId) {

		StringBuilder s = new StringBuilder();
		s.append("SPARQL INSERT INTO GRAPH <").append(event.getGraph().toString()).append("> {\n");
		for (Quadruple quadruple : event) {
			s.append(TypeConversion.toRDF2Go(quadruple.getSubject()).toSPARQL()).append(" ");
			s.append(TypeConversion.toRDF2Go(quadruple.getPredicate()).toSPARQL()).append(" ");
			s.append(TypeConversion.toRDF2Go(quadruple.getObject()).toSPARQL()).append(" . \n");
		}
		s.append("}\n");
		String query = s.toString();
		
		logger.debug("Putting event in cloud {}:\n{}", cloudId, query);
		try {
			Statement st = virtuosoConnection.createStatement();
			st.executeUpdate(query);
		} catch (SQLException e) {
			logger.error("Error putting an event into Virtuoso.", e);
		}
		
   	}

	/**
	 * Retreive data from historic storage using a SPARQL SELECT query. SPARQL 1.1
	 * enhancements like the VALUES clause are allowed.
	 */
	@Override
	public SelectResults getDataFromCloud(String query, String cloudId)
			throws EcConnectionmanagerException {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}

		logger.debug("Sending historical query to Virtuoso: \n{}", query);

		List<String> variables = new ArrayList<String>();
		List<List> result = new ArrayList<List>();

		Connection con = null;
		ResultSet res = null;
		try {
			con = virtuosoConnection;
			Statement sta = con.createStatement();
			res = sta.executeQuery("sparql "+query);

			ResultSetMetaData rmd = res.getMetaData();
			int colNum = rmd.getColumnCount();
			for(int i = 1; i <= colNum; i++){
				variables.add(rmd.getColumnName(i));
			}
			logger.debug("Vars: {}", variables);

			//TODO result create, select variable analyze, create
			while(res.next()){
				ArrayList<Object> data = new ArrayList<Object>();
				for(int i = 1; i <= colNum; i++) {
					data.add(res.getObject(i));
				}
				result.add(data);
				logger.debug("Data: {}", data);
			}

		} catch (SQLException e) {
			throw new EcConnectionmanagerException("Exception with Virtuoso.", e);
		} finally {
			try {
				if (res != null) {
					res.close();
				}
				if(con != null) {
					con.close();
				}
			} catch (SQLException e) {
				// Do nothing
			}
		}

		ResultRegistry rr = new ResultRegistry();
		rr.setResult(result);
		rr.setVariables(variables);
		return rr;
	}

	@Override
	public void publish(CompoundEvent event) {
		if (!init) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialized.");
		}
		
		String cloudId = EventCloudHelpers.getCloudId(event);
	    
		if (!cloudId.isEmpty()) {
			// Send event to DSB:
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RDFDataMgr.write(out, quadruplesToDatasetGraph(event), RDFFormat.TRIG_BLOCKS);
	
			// Do not remove this line, needed for logs. :stuehmer
			logger.info(LOG_DCEP_EXIT + event.getGraph() + " " + EventCloudHelpers.getMembers(event));
			if (logger.isDebugEnabled()) {
				logger.debug(LOG_DCEP + "Complex Event:\n{}", event.toString());
			}
			
			this.getRdfSender().notify(new String(out.toByteArray()), cloudId);
			
			// Store event in Triple Store:
			this.putDataInCloud(event, cloudId);
		}
		else {
			logger.warn(LOG_DCEP_FAILED_EXIT + "Got empty cloud ID from event '{}', don't know which cloud to publish to. Discarding complex event.", event.getGraph() + Event.EVENT_ID_SUFFIX);
		}
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
	private static DatasetGraph quadruplesToDatasetGraph(CompoundEvent quads) {
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
