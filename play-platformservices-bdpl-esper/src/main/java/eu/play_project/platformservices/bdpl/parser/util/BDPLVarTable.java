/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;


import java.util.HashSet;
import java.util.Set;

/**
 * @author ningyuan 
 * 
 * Jul 27, 2014
 *
 */
public class BDPLVarTable {
	
	/*
	 * variables that appear in construct clause ( never be null )
	 */
	private Set<String> constructVars = new HashSet<String>();
	
	/*
	 * common variables in real time pattern ( never be null ) 
	 */
	private Set<String> realTimeCommonVars = new HashSet<String>();

	public Set<String> getConstructVars() {
		return this.constructVars;
	}

	public Set<String> getRealTimeCommonVars() {
		return this.realTimeCommonVars;
	}
}
