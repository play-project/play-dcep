/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import java.util.Map;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;

/**
 * @author ningyuan 
 * 
 * Aug 12, 2014
 *
 */
public class VariableBinder {
	
	private Map<String, String[]> vars;
	
	private final BDPLArrayTable arrayVars;
	
	public VariableBinder(BDPLArrayTable arrayTable){
		arrayVars = arrayTable;
	}
	
	public void setVars(Map<String, String[]> vars) {
		this.vars = vars;
	}
	
	/*
	 * return the first value of the variable. return null if it dose not exist
	 */
	String getVar(String name){
		// vars must not be null
		String[] ret = vars.get(name);
		if(ret != null && ret.length > 0){
			return ret[0];
		}
		else{
			return null;
		}
	}
	
	String[][][] getArray(String name){
		BDPLArrayTableEntry entry = arrayVars.get(name);
		if(entry != null){
			// entry.getArray() must not be null
			return entry.getArray().read();
		}
		else{
			return null;
		}
	}
}
