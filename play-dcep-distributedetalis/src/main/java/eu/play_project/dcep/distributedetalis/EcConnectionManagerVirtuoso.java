package eu.play_project.dcep.distributedetalis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoDataSource;
import eu.play_project.dcep.distributedetalis.join.ResultRegistry;
import eu.play_project.dcep.distributedetalis.join.SelectResults;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;

public class EcConnectionManagerVirtuoso extends EcConnectionManagerNet {
	private VirtuosoDataSource ds;
	private Logger logger = LoggerFactory.getLogger(EcConnectionManagerVirtuoso.class);
	
	public EcConnectionManagerVirtuoso() throws NamingException{
		Properties constants = Constants.getProperties("play-dcep-distribution.properties");
				
		ds = new VirtuosoDataSource();
		ds.setServerName(constants.getProperty("dcep.virtuoso.servername"));
		ds.setPortNumber(Integer.parseInt(constants.getProperty("dcep.virtuoso.port")));
		ds.setUser(constants.getProperty("dcep.virtuoso.user"));
		ds.setPassword(constants.getProperty("dcep.virtuoso.password"));
	}
	
	public EcConnectionManagerVirtuoso(String server, int port, String user, String pw){
		ds = new VirtuosoDataSource();
		ds.setServerName(server);
		ds.setPortNumber(port);
		ds.setUser(user);
		ds.setPassword(pw);
	}
	
	@Override
	public synchronized SelectResults getDataFromCloud(String query, String cloudId) 
			throws EventCloudIdNotManaged, MalformedSparqlQueryException
			{
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
			throws EventCloudIdNotManaged {
		// TODO Auto-generated method stub
		return super.getHistoricCloud(cloudId);
	}

	@Override
	public SubscribeApi getInputCloud(String cloudId)
			throws EventCloudIdNotManaged {
		// TODO Auto-generated method stub
		return super.getInputCloud(cloudId);
	}

	@Override
	public PublishApi getOutputCloud(String cloudId)
			throws EventCloudIdNotManaged {
		// TODO Auto-generated method stub
		return super.getOutputCloud(cloudId);
	}

	@Override
	public void publish(CompoundEvent event) {
		// TODO Auto-generated method stub
		super.publish(event);
	}

	@Override
	public void registerEventPattern(EpSparqlQuery epSparqlQuery) {
		// TODO Auto-generated method stub
		super.registerEventPattern(epSparqlQuery);
	}

	@Override
	public void unregisterEventPattern(EpSparqlQuery epSparqlQuery) {
		// TODO Auto-generated method stub
		super.unregisterEventPattern(epSparqlQuery);
	}

	@Override
	public Subscription subscribe(String cloudId) {
		// TODO Auto-generated method stub
		return super.subscribe(cloudId);
	}

	@Override
	public void unsubscribe(String cloudId, Subscription sub) {
		// TODO Auto-generated method stub
		super.unsubscribe(cloudId, sub);
	}

	private static final long serialVersionUID = 1L; 

}
