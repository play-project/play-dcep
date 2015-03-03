/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.util;

import java.util.List;
import java.util.Set;

import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTableEntry;

/**
 * The class maintains important data related with real-time solutions of a bdpl
 * query. The object of this class is passed to prepared query statement as injected
 * parameter. It can be concerned as a global data to a query.
 * 
 * 
 * 
 * @author ningyuan 
 * 
 * Aug 4, 2014
 *
 */
public class RealTimeSolutionBindingData {
	
	/*
	 * common variables in real time event pattern
	 */
	private final Set<String> realTimeCommonVars;
	
	/*
	 * container of real time results
	 */
	private final RealTimeSolutionSequence results; 
	
	/*
	 * dynamic arrays to be fed
	 */
	private final List<SubQueryTableEntry> dynamicArrays;

	public RealTimeSolutionBindingData(Set<String> realTimeCommonVars, RealTimeSolutionSequence results, List<SubQueryTableEntry> dynamicArrays){
		this.realTimeCommonVars = realTimeCommonVars;
		this.results = results;
		this.dynamicArrays = dynamicArrays;
	}
	
	public Set<String> getRealTimeCommonVars() {
		return this.realTimeCommonVars;
	}

	public RealTimeSolutionSequence getResults() {
		return this.results;
	}

	public List<SubQueryTableEntry> getDynamicArrays() {
		return this.dynamicArrays;
	}
	
}
