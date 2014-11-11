/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ningyuan 
 * 
 * Jul 29, 2014
 *
 */
public class BDPLQueryMap {
	
	private static BDPLQueryMap instance;
	
	private AtomicBoolean lock;
	
	private Map<Long, IBDPLQuery> map;
	
	
	private BDPLQueryMap(){
		map = new HashMap<Long, IBDPLQuery>();
		lock = new AtomicBoolean(true);
	}
	
	public static BDPLQueryMap getInstance(){
		if(instance != null)
			return instance;
		else{
			instance = new BDPLQueryMap();
			return instance;
		}
	}
	
	public IBDPLQuery get(long id){
		
		while(true){
			if(lock.compareAndSet(true, false)){
				try{
					return map.get(id);
				}
				finally{
					lock.set(true);
				}
			}
		}
	}
	
	public boolean set(long id, IBDPLQuery query){
		
		while(true){
			if(lock.compareAndSet(true, false)){
				try{
					if(map.containsKey(id)){
						return false;
					}
					else{
						map.put(id, query);
						return true;
					}
				}
				finally{
					lock.set(true);
				}
			}
		}
	}
	
	public void remove(long id){
		
		while(true){
			if(lock.compareAndSet(true, false)){
				try{
					map.remove(id);
					return;
				}
				finally{
					lock.set(true);
				}
			}
		}
	}
}
