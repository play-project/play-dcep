/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.realtime.util;


import java.util.List;
import java.util.Map;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.BDPLArrayFilter;

/**
 *
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class EPLTranslationData {
	
	/*
	 * the text of prepared epl 
	 */
	private final String epl;
	
	/*
	 * the names of injected parameters according to the parameter sequence
	 */
	private final List<Integer> injectParas;
	
	private final List<BDPLArrayFilter> eventPatternFilters;
	
	/*
	 * the mapping between the name of injected parameter and its concrete object
	 */
	private final Map<Integer, Object> injectParaMapping;
	
	/*
	 * the name of injected parameter "real-time result binding data"
	 */
	public final static int INJECT_PARA_REALTIMERESULT_BINDING_DATA = 0;
	
	public EPLTranslationData(String epl, List<Integer> injectParas, Map<Integer, Object> injectParaMapping, List<BDPLArrayFilter> eventPatternFilters){
		this.epl = epl;
		this.injectParas = injectParas;
		this.eventPatternFilters = eventPatternFilters;
		this.injectParaMapping = injectParaMapping;
	}
	
	public String getEpl() {
		return this.epl;
	}

	public List<Integer> getInjectParams() {
		return this.injectParas;
	}
	
	public List<BDPLArrayFilter> getEventPatternFilters(){
		return eventPatternFilters;
	}
	
	public Map<Integer, Object> getInjectParameterMapping() {
		return injectParaMapping;
	}

}
