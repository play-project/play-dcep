/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author ningyuan 
 * 
 * Aug 6, 2014
 *
 */
public class ExFunctionTable {
	
	private static ExFunctionTable instance;
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    
	private final Lock r = rwl.readLock();
    
    private final Lock w = rwl.writeLock();
    
	private Map<String, ExFunction> table;
	
	private ExFunctionTable(){
		table = new HashMap<String, ExFunction>();
	}
	
	public static ExFunctionTable getInstance(){
		if(instance == null){
			instance = new ExFunctionTable();
			return instance;
		}
		else{
			return instance;
		}
	}
	
	public ExFunction getFunction(String fn){
		try{
			r.lock();
			ExFunction ret = null;
			ret = table.get(fn);
			return ret;
		}
		finally{
			r.unlock();
		}
	}
	
	public void putFunction(String fn, ExFunction f){
		try{
			w.lock();
			table.put(fn, f);
		}
		finally{
			w.unlock();
		}
	}
	
	public String[][] list(){
		try{
			r.lock();
			String [][] list = new String[table.size()][2]; 
			
			int i = 0;
			for(String fn : table.keySet()){
				list[i][0] = fn;
				list[i][1] = table.get(fn).getClassName();
				i++;
			}
			
			return list;
		}
		finally{
			r.unlock();
		}
	}
}
