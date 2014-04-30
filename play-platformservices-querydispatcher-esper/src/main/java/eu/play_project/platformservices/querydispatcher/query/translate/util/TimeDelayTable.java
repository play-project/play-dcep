/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ningyuan 
 * 
 * Apr 7, 2014
 *
 */
public class TimeDelayTable {
	
	private List<TimeDelayEntry> entries = new ArrayList<TimeDelayEntry>();
	
	public int getSize(){
		return entries.size();
	}
	
	public List<TimeDelayEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<TimeDelayEntry> entries) {
		this.entries = entries;
	}
	
	
	public List<TimeDelayEntry> getEntriesByStart(Term start){
		List<TimeDelayEntry> ret = new ArrayList<TimeDelayEntry>();
		
		for(TimeDelayEntry entry : entries){
			if(entry.getStart() == start){
				ret.add(entry);
			}
		}
		
		return ret;
	}
	
	public List<TimeDelayEntry> getEntriesByEnd(Term end){
		List<TimeDelayEntry> ret = new ArrayList<TimeDelayEntry>();
		
		for(TimeDelayEntry entry : entries){
			if(entry.getEnd() == end){
				ret.add(entry);
			}
		}
		
		return ret;
	}
	
	public List<TimeDelayEntry> getEntriesWithStartEvent(){
		List<TimeDelayEntry> ret = new ArrayList<TimeDelayEntry>();
		
		for(TimeDelayEntry entry : entries){
			if(entry.getStart() != null){
				ret.add(entry);
			}
		}
		
		return ret;
	}
}
