/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class TermTable {
	
	private int idCounter = 0;
	
	private Map<Integer, TermEntry> table = new HashMap<Integer, TermEntry>();
	
	public void addTermEntry(TermEntry entry){
		table.put(idCounter++, entry);
	}
	
	public TermEntry getTermEntry(int id){
		return table.get(id);
	}
}
