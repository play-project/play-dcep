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
public class FunctionTable {
	
	private static FunctionTable instance;
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    
	private final Lock r = rwl.readLock();
    
    private final Lock w = rwl.writeLock();
    
	private Map<String, IFunction> table;
	
	private FunctionTable(){
		table = new HashMap<String, IFunction>();
	}
	
	public static FunctionTable getInstance(){
		if(instance == null){
			instance = new FunctionTable();
			return instance;
		}
		else{
			return instance;
		}
	}
	
	public IFunction getFunction(String fn){
		try{
			r.lock();
			IFunction ret = null;
			ret = table.get(fn);
			return ret;
		}
		finally{
			r.unlock();
		}
	}
	
	public void putFunction(String fn, IFunction f){
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
