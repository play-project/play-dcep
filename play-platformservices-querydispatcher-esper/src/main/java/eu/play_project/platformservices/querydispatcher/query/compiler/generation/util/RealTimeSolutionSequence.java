/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.util;


import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



/**
 * @author ningyuan 
 * 
 * Aug 4, 2014
 *
 */
public class RealTimeSolutionSequence {
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    
    
    private int length = 0;
    private RealTimeSolution head, tail;
	
	public RealTimeSolution get(){
		try{
			r.lock();
			RealTimeSolution ret = null;
			if(length > 0){
				ret = head;
				head = head.getNext();
				length--;
				
				if(length == 0){
					tail = null;
				}
			}
			
			return ret;
		}
		finally{
			r.unlock();
		}
	}
	
	public void put(RealTimeSolution add){
		try{
			w.lock();
			if(length < 1){
				head = add;
				tail = head;
				length++;
			}
			else{
				tail.setNext(add);
				tail = tail.getNext();
				length++;
			}
		}
		finally{
			w.unlock();
		}
	}
	
	public static class RealTimeSolution{
    	private RealTimeSolution next = null;
    	
    	private final List<Map<String, String[]>> varBindings;
    	
    	private final Map<String, String[][][]> dynamicArrays;
    	
    	public RealTimeSolution(List<Map<String, String[]>> vbs, Map<String, String[][][]> ays){
    		if(vbs == null || ays == null){
    			throw new IllegalArgumentException();
    		}
    		dynamicArrays = ays;
    		varBindings = vbs;
    	}
    	
    	public List<Map<String, String[]>> getVarBindings(){
    		return varBindings;
    	}
    	
    	public Map<String, String[][][]> getDynamicArrays(){
    		return dynamicArrays;
    	}
    	
    	private RealTimeSolution getNext() {
    		return this.next;
    	}

    	private void setNext(RealTimeSolution next) {
    		this.next = next;
    	}
    }
}
