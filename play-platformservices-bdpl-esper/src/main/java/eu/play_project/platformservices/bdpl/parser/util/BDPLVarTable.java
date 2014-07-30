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
	
	private Set<String> constructVars = new HashSet<String>();
	
	private Set<String> realTimeCommonVars = new HashSet<String>();

	public Set<String> getConstructVars() {
		return this.constructVars;
	}

	public Set<String> getRealTimeCommonVars() {
		return this.realTimeCommonVars;
	}
}
