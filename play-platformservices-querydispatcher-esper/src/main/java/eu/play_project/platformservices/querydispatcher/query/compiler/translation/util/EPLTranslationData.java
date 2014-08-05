/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;


import java.util.List;

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

	public static final int INJECT_PARA_REALTIMERESULT_BINDING_DATA = 0, INJECT_PARA_EXTERNAL_FUNCTION_DATA = 1;
	
	public EPLTranslationData(String epl, List<Integer> injectParas){
		this.epl = epl;
		this.injectParas = injectParas;
	}
	
	public String getEpl() {
		return this.epl;
	}

	public List<Integer> getInjectParams() {
		return this.injectParas;
	}
}
