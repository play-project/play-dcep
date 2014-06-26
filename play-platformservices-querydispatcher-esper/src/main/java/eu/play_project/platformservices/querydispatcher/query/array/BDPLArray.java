/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.array;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLArray {
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    
    // size: static array 0, dynamic array > 0
    private int size = 0, length = 0;
    private BDPLArrayElement head, tail;
    
    public BDPLArray(BDPLArrayElement head){
    	if(head == null){
    		throw new IllegalArgumentException("The initiate content of a static BDPL array should not be null.");
    	}
    	// the sequence of initiation is important
    	this.tail = findTail(head);
    	this.head = head;
    }
    
    public BDPLArray(int size, BDPLArrayElement head){
    	if(size < 1){
    		throw new IllegalArgumentException("The size of a dynamic BDPL array should be greater than 0.");
    	}
    	// the sequence of initiation is important
    	this.size = size;
    	this.tail = findTail(head);
    	this.head = head;
    	
    }
    
    public String[] read(){
    	r.lock();
    	try{
    		
    		String [] ret = new String[length];
	    	BDPLArrayElement current = head;
	    		
	    	int i = 0;
	    	while(current != null){
	    		ret[i++] = current.getContent();
	    		current = current.getNext();
	    	}
	    		
	    	return ret;
    		
    	}
    	finally{
    		r.unlock();
    	}
    }
    
    public void write(String add) throws BDPLArrayException{
    	
    	if(size > 0){
	    	w.lock();
	    	try{
	    		// 
	    		if(length == size){
	    			if(length > 1){
	    				head.setContent(add);
	    				tail.setNext(head);
	    				tail = head;
	    				head = head.getNext();
	    				tail.setNext(null);
	    			}
	    			// length == 1
	    			else{
	    				head.setContent(add);
	    			}
	    		}
	    		else if(length < size){
	    			BDPLArrayElement e = new BDPLArrayElement(add);
	    			
	    			if(length < 1){
	    				head = e;
	    				tail = e;
		    			length++;
	    			}
	    			else{
	    				tail.setNext(e);
	    				tail = e;
		    			length++;
	    			}
	    			
	    		}
	    	}
	    	finally{
	    		w.unlock();
	    	}
    	}
    	else{
    		throw new BDPLArrayException("The content of a static BDPL array could not be changed.");
    	}
    }
    
    private BDPLArrayElement findTail(BDPLArrayElement head){
    	BDPLArrayElement ret = head;
    	BDPLArrayElement current = head;
    	while(current != null){
    		ret = current;
    		length++;
    		current = current.getNext();
    	}
    		
    	if(size > 0 && length > size){
    		throw new IllegalArgumentException("The initiate length of a dynamic BDPL array is greater than its size.");
    	}
    	
    	return ret;
    }
}
