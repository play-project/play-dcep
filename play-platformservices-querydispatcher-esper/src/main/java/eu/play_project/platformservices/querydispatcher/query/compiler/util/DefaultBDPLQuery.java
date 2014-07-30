/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;

import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;

/**
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class DefaultBDPLQuery implements IBDPLQuery{
	
	private final String query;
	
	private final UpdateListener listener;
	
	private final SubQueryTable subQueryTable;
	
	public DefaultBDPLQuery(String query, UpdateListener listener, SubQueryTable subQueryTable){
		this.query = query;
		this.listener = listener;
		this.subQueryTable = subQueryTable;
	}
	
	public String getQuery() {
		return this.query;
	}

	public UpdateListener getListener() {
		return this.listener;
	}

	public SubQueryTable getSubQueryTable() {
		return this.subQueryTable;
	}

	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
