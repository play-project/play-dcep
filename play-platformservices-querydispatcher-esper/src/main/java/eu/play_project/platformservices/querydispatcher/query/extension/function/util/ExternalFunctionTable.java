/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import eu.play_project.platformservices.querydispatcher.query.extension.function.ExFunctionSignature;

/**
 * @author ningyuan 
 * 
 * Aug 6, 2014
 *
 */
public class ExternalFunctionTable {
	
	private static ExternalFunctionTable instance;
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    
	private final Lock r = rwl.readLock();
    
    private final Lock w = rwl.writeLock();
    
	private Map<ExFunctionSignature, Class> table;
	
	private ExternalFunctionTable(){
		table = new HashMap<ExFunctionSignature, Class>();
	}
	
	public static ExternalFunctionTable getInstance(){
		if(instance != null){
			return instance;
		}
		else{
			instance = new ExternalFunctionTable();
			return instance;
		}
	}
	
	public Class getFunctionClass(ExFunctionSignature fn){
		try{
			instance.r.lock();
			Class ret = null;
			ret = instance.table.get(fn);
			return ret;
		}
		finally{
			instance.r.unlock();
		}
	}
	
	public void putFunctionClass(ExFunctionSignature fn, Class f){
		try{
			instance.w.lock();
			instance.table.put(fn, f);
		}
		finally{
			instance.w.unlock();
		}
	}
	
	public String[][] list(){
		try{
			instance.r.lock();
			String [][] list = new String[instance.table.size()][2]; 
			
			int i = 0;
			for(ExFunctionSignature fn : instance.table.keySet()){
				list[i][0] = fn.getName();
				list[i][1] = instance.table.get(fn).getName();
				i++;
			}
			
			return list;
		}
		finally{
			instance.r.unlock();
		}
	}
}
