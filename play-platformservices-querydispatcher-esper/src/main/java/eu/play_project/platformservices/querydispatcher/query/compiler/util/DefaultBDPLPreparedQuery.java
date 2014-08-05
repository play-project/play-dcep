/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;

import java.util.List;
import java.util.Map;

import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;

/**
 * @author ningyuan 
 * 
 * Aug 5, 2014
 *
 */
public class DefaultBDPLPreparedQuery implements IBDPLQuery{
	
	private final String epl;
	
	private final Map<Integer, Object> injectParaMapping;
	
	private final List<Integer> injectParams;

	private final UpdateListener listener;
	
	private final SubQueryTable subQueryTable;
	
	public DefaultBDPLPreparedQuery(String epl, Map<Integer, Object> injectParaMapping, List<Integer> injectParams, UpdateListener listener, SubQueryTable subQueryTable){
		this.epl = epl;
		this.injectParaMapping = injectParaMapping;
		this.injectParams = injectParams;
		this.listener = listener;
		this.subQueryTable = subQueryTable;
	}
	
	public String getEPL() {
		return this.epl;
	}
	
	public Map<Integer, Object> getInjectParaMapping() {
		return this.injectParaMapping;
	}

	public List<Integer> getInjectParams() {
		return this.injectParams;
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
