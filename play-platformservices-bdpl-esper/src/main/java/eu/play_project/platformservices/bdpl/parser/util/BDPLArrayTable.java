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
public class BDPLArrayTable {
	
	private Map<String, BDPLArrayTableEntry> table = new HashMap<String, BDPLArrayTableEntry>();
	
	/**
	 * 
	 * @param name must not be null
	 * @param entry must not be null
	 * @throws BDPLArrayException 
	 */
	public void add(String name, BDPLArrayTableEntry entry) throws BDPLArrayException{
		
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
}
