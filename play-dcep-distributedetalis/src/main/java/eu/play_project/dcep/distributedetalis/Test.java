package eu.play_project.dcep.distributedetalis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import virtuoso.jdbc4.VirtuosoDataSource;

public class Test {

	/**
	 * @param args
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws NamingException{
		// TODO Auto-generated method stub
		VirtuosoDataSource ds = new VirtuosoDataSource();
		ds.setServerName("localhost");
		ds.setPortNumber(1111);
		//ds.setDatabaseName("DB");
		ds.setUser("dba");
		ds.setPassword("dba");
		
		Connection con = null;
		String query = "sparql " +
				"select distinct ?a " +
				"from <http://events.event-processing.org/ids/twitter8054871505879475923> " +
				"where {?b <http://events.event-processing.org/types/twitterName> ?a}";
		try {
			
			con = ds.getConnection();
			
			Statement sta = con.createStatement();
			
			ResultSet res = sta.executeQuery(query);
			
			while(res.next()){
				System.out.println(res.getString("a"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(con != null)
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
}
