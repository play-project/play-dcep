/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util;

import java.util.ArrayList;
import java.util.List;


/**
 * The sub-query table is the data structure which keeps all 
 * sub-queries used for updating arrays of one bdpl query.
 * 
 * 
 * 
 * @author ningyuan 
 * 
 * Jul 1, 2014
 *
 */
public class SubQueryTable {
	
	private List<SubQueryTableEntry> table = new ArrayList<SubQueryTableEntry>();
	
	public void add(SubQueryTableEntry entry){
		table.add(entry);
	}
	
	public List<SubQueryTableEntry> getAll(){
		return table;
	}
	
	public List<SubQueryTableEntry> getEntryToSelf(){
		List<SubQueryTableEntry> ret = new ArrayList<SubQueryTableEntry>();
		
		for(int i = 0; i < table.size(); i++){
			SubQueryTableEntry temp = table.get(i);
			if(temp.getQuery() == null){
				ret.add(temp);
			}
		}
		
		return ret;
	}
}
