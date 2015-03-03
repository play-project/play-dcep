/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.util;


import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



/**
 * The class of real-time solution sequence of a bdpl query. A real time solution
 * contains all real-time variable bindings and all dynamic arrays. While executing
 * a query, a real-time solution may be created by a filter at the end of the pattern
 * matching and be consumed by a listener before joining the real-time data with 
 * historic data.
 * 
 * 
 * 
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
	
    /**
     * Get and remove the real time solution on the head of the solution sequence.
     * 
     * @return
     */
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
	
	/**
	 * Add a new real time solution to the tail of the solution sequence.
	 * 
	 * @param add
	 */
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
	
	/*
	 * the content of real time solution. All variable bindings and all dynamic arrays
	 */
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
