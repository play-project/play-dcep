/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;


import java.util.HashSet;
import java.util.Set;

/**
 * A table of BDPL variables. A data structure created by compiler. It contains
 * two sets of variable names. One is the set of variable names in construct
 * clause. The other is the set of common variable names of real time event pattern.
 * 
 * 
 * @author ningyuan 
 * 
 * Jul 27, 2014
 *
 */
public class BDPLVarTable {
	
	/*
	 * variables that appear in construct clause
	 */
	private Set<String> constructVars = new HashSet<String>();
	
	/*
	 * common variables in real time pattern
	 */
	private Set<String> realTimeCommonVars = new HashSet<String>();

	/**
	 * Get all variable names in construct clause of query
	 * 
	 * @return (never be null)
	 */
	public Set<String> getConstructVars() {
		return this.constructVars;
	}
	
	/**
	 * Get common variable names of real time event pattern
	 * 
	 * @return (never be null)
	 */
	public Set<String> getRealTimeCommonVars() {
		return this.realTimeCommonVars;
	}
}
