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
	
	private Map<String, String[][][]> dArrays;
	
	private final BDPLArrayTable arrayVars;
	
	public VariableBinder(BDPLArrayTable arrayTable){
		arrayVars = arrayTable;
	}
	
	public BDPLArrayTable getArrayTable(){
		return arrayVars;
	}
	
	/**
	 * set variable values for evaluating
	 * 
	 * @param vars (must not be null)
	 */
	public void bindVariableValues(Map<String, String[]> vars, Map<String, String[][][]> dArrays) {
		this.vars = vars;
		this.dArrays = dArrays;
	}
	
	/*
	 * return the value of the variable. return null if it dose not exist
	 */
	String getVarValue(String name){
		// vars must not be null
		String[] ret = vars.get(name);
		if(ret != null && ret.length > 1){
			return ret[1];
		}
		else{
			return null;
		}
	}
	
	String[][][] getArray(String name){
		String [][][] ret = null;
		if(dArrays != null)
			ret = dArrays.get(name);
		
		if(ret == null){
			BDPLArrayTableEntry entry = arrayVars.get(name);
			if(entry != null){
				// entry.getArray() must not be null
				return entry.getArray().read();
			}
		}
		
		return ret;
	}
}
