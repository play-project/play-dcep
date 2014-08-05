/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.util;


import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author ningyuan 
 * 
 * Aug 4, 2014
 *
 */
public class RealTimeResults {
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    
    
    private int length = 0;
    private RealTimeResult head, tail;
	
	public Map<String, String> get(){
		try{
			r.lock();
			Map<String, String> ret = null;
			if(length > 0){
				ret = head.getContent();
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
	
	public void put(Map<String, String> content){
		try{
			w.lock();
			if(length < 1){
				head = new RealTimeResult(content);
				tail = head;
				length++;
			}
			else{
				RealTimeResult add = new RealTimeResult(content);
				tail.setNext(add);
				tail = tail.getNext();
				length++;
			}
		}
		finally{
			w.unlock();
		}
	}
	
	private static class RealTimeResult{
    	private RealTimeResult next = null;
    	
    	private Map<String, String> content;
    	
    	public RealTimeResult(Map<String, String> content){
    		if(content == null){
    			throw new IllegalArgumentException();
    		}
    		this.content = content;
    	}
    	
    	public Map<String, String> getContent(){
    		return content;
    	}

    	public RealTimeResult getNext() {
    		return this.next;
    	}

    	public void setNext(RealTimeResult next) {
    		this.next = next;
    	}
    }
}
