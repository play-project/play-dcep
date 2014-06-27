/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class ArrayTable {
	
	private Map<String, ArrayTableEntry> table = new HashMap<String, ArrayTableEntry>();
	
	/**
	 * 
	 * @param name must not be null
	 * @param entry must not be null
	 * @throws BDPLArrayException 
	 */
	public void add(String name, ArrayTableEntry entry) throws BDPLArrayException{
		
		if(!table.containsKey(name)){
			table.put(name, entry);
		}
		else{
			throw new BDPLArrayException("Duplicated array variable name "+name+".");
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public ArrayTableEntry get(String name){
		return table.get(name);
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<String> keySet(){
		return table.keySet();
	}
}
