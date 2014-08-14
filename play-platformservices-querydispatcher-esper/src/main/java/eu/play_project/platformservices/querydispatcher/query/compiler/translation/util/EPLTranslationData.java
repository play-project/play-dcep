/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;


import java.util.List;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.ArrayFilter;

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
	
	private final List<ArrayFilter> arrayFilters;
	
	public static final int INJECT_PARA_REALTIMERESULT_BINDING_DATA = 0, INJECT_PARA_EXTERNAL_FUNCTION_DATA = 1;
	
	public EPLTranslationData(String epl, List<Integer> injectParas, List<ArrayFilter> arrayFilters){
		this.epl = epl;
		this.injectParas = injectParas;
		this.arrayFilters = arrayFilters;
	}
	
	public String getEpl() {
		return this.epl;
	}

	public List<Integer> getInjectParams() {
		return this.injectParas;
	}
	
	public List<ArrayFilter> getArrayFilters() {
		return this.arrayFilters;
	}

}
