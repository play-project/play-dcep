/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.array;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;

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
    
    // size: static array -1, dynamic array > 0
    private int size = -1, length = 0;
    private BDPLArrayElement head, tail;
    
    public BDPLArray(BDPLArrayElement head){
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
    
    public Object[][] read(){
    	r.lock();
    	try{
    		
    		Object [][] ret = new Object[length][];
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
    
    /**
     * 
     * @param add 
     * @throws BDPLArrayException
     */
    public void write(Object[] add) throws BDPLArrayException{
    	
    	if(size > -1){
    		
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
    
    /**
     * 
     * 
     * @param adds 
     * @throws BDPLArrayException
     */
    public void write(Object [][] adds) throws BDPLArrayException{
    	
    	if(size > -1){
	    	w.lock();
	    	try{
	    		if(length + adds.length > size){
	    			throw new BDPLArrayException("Writing too many elements into  a dynamic BDPL array.");
	    		}
	    		else{
	    			
		    		BDPLArrayElement e = new BDPLArrayElement(adds[0]);
		    			
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
		    			
		    		for(int i = 1; i < adds.length; i++){
	    				e = new BDPLArrayElement(adds[i]);
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
    		
    	if(size > -1 && length > size){
    		throw new IllegalArgumentException("The initiate length of a dynamic BDPL array is greater than its size.");
    	}
    	
    	return ret;
    }
}
