/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util;


import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLQuery;

/**
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public class SubQueryTableEntry {
	
	private IBDPLQuery query;
	
	private BDPLArray array;
	
	private String[] selectedVars;

	public IBDPLQuery getQuery() {
		return this.query;
	}

	public void setQuery(IBDPLQuery query) {
		this.query = query;
	}

	public BDPLArray getArray() {
		return this.array;
	}

	public void setArray(BDPLArray array) {
		this.array = array;
	}

	public String[] getSelectedVars() {
		return this.selectedVars;
	}

	public void setSelectedVars(String[] selectedVars) {
		this.selectedVars = selectedVars;
	}
}
