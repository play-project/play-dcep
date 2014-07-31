/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation;

import java.util.List;

/**
 * Not used
 * 
 * 
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class TempReturn {
	
	private String epl;
	
	private List<String> matchedPatternSparql;
	
	public TempReturn(String epl, List<String> matchedPatternSparql){
		this.epl = epl;
		this.matchedPatternSparql = matchedPatternSparql;
	}
	
	public String getEpl() {
		return this.epl;
	}

	public List<String> getMatchedPatternSparql() {
		return this.matchedPatternSparql;
	}
}
