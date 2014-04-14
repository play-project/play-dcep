/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ningyuan 
 * 
 * Apr 10, 2014
 *
 */
public class BDPLTransformerUtil {
	
	public static final int TERM_EVENT = 0, TERM_TIME = 1;
	
	public static long getDurationInSec(String duration) throws BDPLTransformException{
		return Long.valueOf(duration);
	}
	
	public static int getTermType(Term term){
		if(term.getName().contains("event")){
			return TERM_EVENT;
		}
		else if(term.getName().contains("interval")){
			return TERM_TIME;
		}
		else{
			return -1;
		}
	}
	
	/**
	 * Sort time delay entries ascendingly by the sequence of start term in terms.
	 * 
	 * @param entries
	 * @param terms
	 */
	public static void sortTimeDelayEntryByStart(List<TimeDelayEntry> entries, SeqClause seqc){
		
		// sort time delay entries ascendingly by the sequence of start term in terms
		List<Term> terms = seqc.getTerms();
		
		int size = entries.size();
		for(int i = 0; i < size - 1; i++){
			for(int j = 0; j < size - i - 1; j++){
						
				Term t1 = entries.get(j).getStart(), t2 = entries.get(j+1).getStart();
				int i1 = Integer.MIN_VALUE, i2 = Integer.MIN_VALUE;
						
				if(t1 != null){
					i1 = terms.indexOf(t1);
						
				}
				if(t2 != null){
					i2 = terms.indexOf(t2);
						
				}
						
				if(i1 > i2){
					Collections.swap(entries, j, j+1);
						
				}
				}
			}
	}
	
	/**
	 * Sort time delay entries ascendingly by the sequence of end term in terms.
	 * 
	 * @param entries
	 * @param terms
	 */
	public static void sortTimeDelayEntryByEnd(List<TimeDelayEntry> entries, SeqClause seqc){
		
		// sort time delay entries ascendingly by the sequence of end term in terms
		List<Term> terms = seqc.getTerms();
		
		int size = entries.size();
		for(int i = 0; i < size - 1; i++){
			for(int j = 0; j < size - i - 1; j++){
						
				Term t1 = entries.get(j).getEnd(), t2 = entries.get(j+1).getEnd();
				int i1 = Integer.MAX_VALUE, i2 = Integer.MAX_VALUE;
						
				if(t1 != null){
					i1 = terms.indexOf(t1);
				}
				if(t2 != null){
					i2 = terms.indexOf(t2);
				}
						
				if(i1 > i2){
					Collections.swap(entries, j, j+1);
				}
			}
		}
	}
	
	/**
	 * 
	 * 
	 * @param entries
	 * @param seqc the content should not be changed
	 */
	public static void reduceStartDelayEntry(List<TimeDelayEntry> entries, SeqClause seqc, boolean dele, TimeDelayTable tdTable){
		
		// sort time delay entries ascendingly by the sequence of end term in terms
		List<Term> terms = seqc.getTerms();
		int size = entries.size();
		for(int i = 0; i < size - 1; i++){
			for(int j = 0; j < size - i - 1; j++){
				
				Term t1 = entries.get(j).getEnd(), t2 = entries.get(j+1).getEnd();
				int i1 = Integer.MAX_VALUE, i2 = Integer.MAX_VALUE;
				
				if(t1 != null){
					i1 = terms.indexOf(t1);
				}
				if(t2 != null){
					i2 = terms.indexOf(t2);
				}
				
				if(i1 > i2){
					Collections.swap(entries, j, j+1);
				}
			}
		}
		
		// reduce
		List<Integer> removeIndex = new ArrayList<Integer>();
		int last = 0, next = last+1;
		while(next < entries.size()){
			if(entries.get(last).getDuration() >= entries.get(next).getDuration()){
				removeIndex.add(next);
				next++;
			}
			else{
				last = next;
				next++;
			}
		}
		
		if(dele){
			List<TimeDelayEntry> tdEntries = tdTable.getEntries();
			for(int i = 0; i < removeIndex.size(); i++){
				tdEntries.remove(entries.get(removeIndex.get(i)));
			}
		}
		
		for(int i = 0; i < removeIndex.size(); i++){
			entries.remove(removeIndex.get(i)-i);
		}
	}
	
	/**
	 * 
	 * 
	 * @param entries
	 * @param seqc the content should not be changed
	 */
	public static void reduceEndDelayEntry(List<TimeDelayEntry> entries, SeqClause seqc, boolean dele, TimeDelayTable tdTable){
		
		// sort time delay entries ascendingly by the sequence of end term in terms
		List<Term> terms = seqc.getTerms();
		int size = entries.size();
		for(int i = 0; i < size - 1; i++){
			for(int j = 0; j < size - i - 1; j++){
				
				Term t1 = entries.get(j).getStart(), t2 = entries.get(j+1).getStart();
				int i1 = Integer.MIN_VALUE, i2 = Integer.MIN_VALUE;
				
				if(t1 != null){
					i1 = terms.indexOf(t1);
				}
				if(t2 != null){
					i2 = terms.indexOf(t2);
				}
				
				if(i1 > i2){
					Collections.swap(entries, j, j+1);
				}
			}
		}
		
		// reduce
		List<Integer> removeIndex = new ArrayList<Integer>();
		int last = entries.size()-1, next = last-1;
		while(next > -1){
			if(entries.get(last).getDuration() >= entries.get(next).getDuration()){
				removeIndex.add(next);
				next--;
			}
			else{
				last = next;
				next--;
			}
		}
		
		if(dele){
			List<TimeDelayEntry> tdEntries = tdTable.getEntries();
			for(int i = 0; i < removeIndex.size(); i++){
				tdEntries.remove(entries.get(removeIndex.get(i)));
			}
		}
		
		for(int i = 0; i < removeIndex.size(); i++){
			entries.remove(removeIndex.get(i)-i);
		}
	}
}
