/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A table of BDPL arrays. A data structure created by compiler for gathering
 * and keeping information of all BDPL arrays in a BDPL query.
 * 
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLArrayTable {
	
	private Map<String, BDPLArrayTableEntry> table = new HashMap<String, BDPLArrayTableEntry>();
	
	/**
	 * Add information of a BDPL array.
	 * 
	 * @param name name of array variable
	 * @param entry information of BDPL array
	 * @throws BDPLArrayException 
	 */
	public void add(String name, BDPLArrayTableEntry entry) throws BDPLArrayException{
		if(name == null){
			throw new IllegalArgumentException();
		}
		if(entry == null){
			throw new IllegalArgumentException();
		}
		
		if(!table.containsKey(name)){
			table.put(name, entry);
		}
		else{
			throw new BDPLArrayException("Duplicated array variable name "+name+".");
		}
	}
	
	/**
	 * Get information of a BDPL array with name.
	 * 
	 * @param name
	 * @return
	 */
	public BDPLArrayTableEntry get(String name){
		return table.get(name);
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<String> keySet(){
		return table.keySet();
	}
	
	public boolean contain(String name){
		return table.containsKey(name);
	}
}
