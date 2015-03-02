/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import java.util.Map;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;

/**
 * The data holder for the latest values of variables used in  
 * real-time query. Because the value of a variable may change 
 * while evaluating a real-time query, it is necessary to create 
 * a class for saving and binding variables.
 * 
 * 
 * @author ningyuan 
 * 
 * Aug 12, 2014
 *
 */
public class VariableBinder {
	
	/*
	 * values of simple variables in query 
	 */
	private Map<String, String[]> vars;
	
	/*
	 * values of dynamic arrays in query
	 */
	private Map<String, String[][][]> dArrays;
	
	/*
	 * values of static arrays in query
	 */
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
