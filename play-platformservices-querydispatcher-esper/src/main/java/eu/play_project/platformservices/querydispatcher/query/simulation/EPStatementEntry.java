/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;

import com.espertech.esper.client.EPStatement;

/**
 * @author ningyuan 
 * 
 * Apr 30, 2014
 *
 */
public class EPStatementEntry {
	
	private final String epl;
	
	private final EPStatement statement;
	
	public EPStatementEntry(String e, EPStatement s){
		epl = e;
		statement = s;
	}
	
	public String getEpl() {
		return this.epl;
	}

	public EPStatement getStatement() {
		return this.statement;
	}

	
}
