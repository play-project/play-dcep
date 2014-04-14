/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ningyuan 
 * 
 * Apr 4, 2014
 *
 */
public class Table {
	
	private List<Entry> entries = new ArrayList<Entry>();
	
	public void addEntry(Entry entry){
		entries.add(entry);
	}
	
	public Entry getEntryByNotStart(Term start){
		Entry ret = null;
		for(int i = 0; i < entries.size(); i++){
			Entry temp = entries.get(i);
			if(temp.getNotStart() == start){
				ret = temp;
				break;
			}
		}
		return ret;
	}
	
	public Entry getEntryByNotEnd(Term end){
		Entry ret = null;
		for(int i = 0; i < entries.size(); i++){
			Entry temp = entries.get(i);
			if(temp.getNotEnd() == end){
				ret = temp;
				break;
			}
		}
		return ret;
	}
}
