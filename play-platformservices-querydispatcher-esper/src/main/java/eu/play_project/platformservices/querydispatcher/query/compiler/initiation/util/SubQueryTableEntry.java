/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util;


import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.IBDPLQuery;

/**
 * The collection data of a sub-query.
 * 
 * 
 * 
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public class SubQueryTableEntry {
	
	/*
	 * sub query object. If it is null, the sub query is the query it self
	 */
	private IBDPLQuery query;
	
	/*
	 * the array into which the sub query inserts variables
	 */
	private BDPLArrayTableEntry arrayEntry;
	
	/*
	 * the method used to parse a sparql literal into an array
	 */
	private String toArrayMethod = null;
	
	/*
	 * the selected variables of the sub query
	 */
	private String[] selectedVars;

	public IBDPLQuery getQuery() {
		return this.query;
	}

	public void setQuery(IBDPLQuery query) {
		this.query = query;
	}

	public BDPLArrayTableEntry getArrayEntry() {
		return this.arrayEntry;
	}

	public void setArrayEntry(BDPLArrayTableEntry arrayEntry) {
		this.arrayEntry = arrayEntry;
	}

	public String[] getSelectedVars() {
		return this.selectedVars;
	}

	public void setSelectedVars(String[] selectedVars) {
		this.selectedVars = selectedVars;
	}
	
	public String getToArrayMethod() {
		return this.toArrayMethod;
	}

	public void setToArrayMethod(String toArrayMethod) {
		this.toArrayMethod = toArrayMethod;
	}

}
