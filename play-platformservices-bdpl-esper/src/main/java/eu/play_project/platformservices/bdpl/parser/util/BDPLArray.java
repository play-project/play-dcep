/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



/**
 * The class of BDPL array. An array is an object of String[length][dimension][2]. 
 * If an element of array is a RDF literal, then in String[][][0] the label is saved.
 * In String[][][1] the whole literal. For other cases the element is saved in String[][][0].
 * 
 * The read() method is protected by a read-lock, and write() by a write-lock;
 * 
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLArray {
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    
    /*
     *  size: static array -1 
     *        dynamic array > 0
     */
    private int size = -1, length = 0;
    private BDPLArrayElement head, tail;
    
    /*
     * dimension of this array
     */
    private int dimension = 0;

	/**
     * Constructor of static BDPL array
     * 
     * @param content
     */
    public BDPLArray(String[][][] content){
    	if(content != null){
    		if(content.length == 0){
	    		tail = null;
	    		head = null;
	    	}
	    	else if(content.length > 1){
	    		head = new BDPLArrayElement(content[0]);
	    		tail = head;
	    		length = 1;
	    		for(int i = 1; i < content.length; i++){
	    			BDPLArrayElement temp = new BDPLArrayElement(content[i]);
	    			tail.setNext(temp);
	    			tail = temp;
	    			length++;
	    		}
	    	}
	    	else{
	    		head = new BDPLArrayElement(content[0]);
	    		tail = head;
	    		length = 1;
	    	}
    	}
    	else{
    		tail = null;
    		head = null;
    	}
    }
    
    /**
     * Constructor of dynamic BDPL array
     * 
     * @param size
     * @param content
     */
    public BDPLArray(int size, String[][][] content){
    	if(size < 1){
    		throw new IllegalArgumentException("The size of a dynamic BDPL array should be greater than 0.");
    	}
    	
    	if(content != null){
    		if(size < content.length){
    			throw new IllegalArgumentException("The initiate length of a dynamic BDPL array is greater than its size.");
    		}
    		else{
    			if(content.length == 0){
    	    		tail = null;
    	    		head = null;
    	    	}
    	    	else if(content.length > 1){
    	    		head = new BDPLArrayElement(content[0]);
    	    		tail = head;
    	    		length = 1;
    	    		for(int i = 1; i < content.length; i++){
    	    			BDPLArrayElement temp = new BDPLArrayElement(content[i]);
    	    			tail.setNext(temp);
    	    			tail = temp;
    	    			length++;
    	    		}
    	    	}
    	    	else{
    	    		head = new BDPLArrayElement(content[0]);
    	    		tail = head;
    	    		length = 1;
    	    	}
    		}
    	}
    	else{
    		tail = null;
    		head = null;
    	}
    	
    	this.size = size;
    }
    
    public int getDimension() {
		return this.dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
	
    public int length(){
    	return length;
    }
    
    /**
     * Read all elements in the array
     * 
     * @return all elements in the array. (never be null)
     */
    public String[][][] read(){
    	
    	try{
    		r.lock();
    		String [][][] ret = new String[length][][];
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
     * Write an element into the array.
     * 
     * @param add (must not be null)
     * @throws BDPLArrayException
     */
    public void write(String[][] add) throws BDPLArrayException{
    	if(add.length == 0){
    		return;
    	}
    	
    	// dynamic array
    	if(size > -1){
    		try{
	    		w.lock();
	    		// array is full
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
	    		// array is not full
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
     * Write many elements into the array.
     * 
     * @param adds (must not be null)
     * @throws BDPLArrayException
     */
    public void write(String [][][] adds) throws BDPLArrayException{
    	if(adds.length == 0){
    		return;
    	}
    	
    	// dynamic array
    	if(size > -1){
	    	
	    	try{
	    		w.lock();
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
    
    /*
     * Inner wrapper class for bdpl array element
     */
    private static class BDPLArrayElement{
    	
    	private BDPLArrayElement next = null;
    	
    	/*
    	 * content[dimension][0] = label value
    	 * content[dimension][1] = whole value
    	 */
    	private String[][] content;
    	
    	private BDPLArrayElement(String[][] content){
    		if(content == null){
    			throw new IllegalArgumentException();
    		}
    		this.content = content;
    	}
    	
    	private String[][] getContent(){
    		return content;
    	}
    	
    	private void setContent(String[][] content){
    		if(content == null){
    			throw new IllegalArgumentException();
    		}
    		this.content = content;
    	}

    	private BDPLArrayElement getNext() {
    		return this.next;
    	}

    	private void setNext(BDPLArrayElement next) {
    		this.next = next;
    	}
    }
}
