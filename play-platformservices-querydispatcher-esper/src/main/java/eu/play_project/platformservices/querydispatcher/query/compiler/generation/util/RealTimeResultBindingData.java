/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.util;

import java.util.List;
import java.util.Set;

import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTableEntry;

/**
 * @author ningyuan 
 * 
 * Aug 4, 2014
 *
 */
public class RealTimeResultBindingData {
	
	private final Set<String> realTimeCommonVars;
	
	private final RealTimeResults results; 
	
	private final List<SubQueryTableEntry> dynamicArrays;

	public RealTimeResultBindingData(Set<String> realTimeCommonVars, RealTimeResults results, List<SubQueryTableEntry> dynamicArrays){
		this.realTimeCommonVars = realTimeCommonVars;
		this.results = results;
		this.dynamicArrays = dynamicArrays;
	}
	
	public Set<String> getRealTimeCommonVars() {
		return this.realTimeCommonVars;
	}

	public RealTimeResults getResults() {
		return this.results;
	}

	public List<SubQueryTableEntry> getDynamicArrays() {
		return this.dynamicArrays;
	}
	
}
