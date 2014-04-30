/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ningyuan 
 * 
 * Apr 4, 2014
 *
 */
public class NotTable {
	
	private List<NotEntry> entries = new ArrayList<NotEntry>();
	
	public void addEntry(NotEntry entry){
		entries.add(entry);
	}
	
	public NotEntry getEntryByNotStart(Term start){
		NotEntry ret = null;
		for(int i = 0; i < entries.size(); i++){
			NotEntry temp = entries.get(i);
			if(temp.getNotStart() == start){
				ret = temp;
				break;
			}
		}
		return ret;
	}
	
	public NotEntry getEntryByNotEnd(Term end){
		NotEntry ret = null;
		for(int i = 0; i < entries.size(); i++){
			NotEntry temp = entries.get(i);
			if(temp.getNotEnd() == end){
				ret = temp;
				break;
			}
		}
		return ret;
	}
}
