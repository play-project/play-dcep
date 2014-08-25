/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;


import java.util.List;
import java.util.Map;

import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLFilter;

/**
 *
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class EPLTranslationData {
	
	private final String epl;
	
	private final List<Integer> injectParas;
	
	private final List<IBDPLFilter<Map<String, String[]>>> eventPatternFilters;
	
	private final Map<Integer, Object> injectParaMapping;
	
	public final static int INJECT_PARA_REALTIMERESULT_BINDING_DATA = 0;
	
	public EPLTranslationData(String epl, List<Integer> injectParas, Map<Integer, Object> injectParaMapping, List<IBDPLFilter<Map<String, String[]>>> eventPatternFilters){
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
	
	public List<IBDPLFilter<Map<String, String[]>>> getEventPatternFilters(){
		return eventPatternFilters;
	}
	
	public Map<Integer, Object> getInjectParameterMapping() {
		return injectParaMapping;
	}

}
