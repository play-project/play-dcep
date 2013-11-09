package eu.play_project.dcep.distributedetalis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.rdf2go.impl.jena.TypeConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoDataSource;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class EcConnectionManagerVirtuoso extends EcConnectionManagerWsn {
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

		init();
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
}
