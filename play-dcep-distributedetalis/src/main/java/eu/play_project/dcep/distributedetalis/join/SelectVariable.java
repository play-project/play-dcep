package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a data structure that is used to represent a selected variable in queries. It's attribute
 * relIndex indicates how many queries have selected this variable and in relRes all these relevant 
 * queries are stored in decreased oder on size of query result.
 * 
 * 
 * @author Ningyuan Pan
 *
 */

public class SelectVariable <T>{
	
	// index of how many queries select this variable
	private int relIndex = 0;
	private List<T> values;
	
	// queries that select this variable
	private TreeSet<ResultRegistry> relRes = new TreeSet<ResultRegistry>(new Comparator<ResultRegistry>(){
		@Override
		public int compare(ResultRegistry o1, ResultRegistry o2) {
			return o1.getSize() - o2.getSize();
		}
	});
	
	private Logger logger = LoggerFactory.getLogger(SelectVariable.class);
	
	public int getRelevantIndex(){
		return relIndex;
	}
	
	public List<T> getValues(){
		return values;
	}
	
	public boolean addRelResult(ResultRegistry r){
		boolean done = relRes.add(r);
		if(done){
			relIndex++;
			//logger.debug("addResult(): add "+r.getNum()+" relIndex"+relIndex);
		}
		return done;
	}
	
	public boolean removeRelResult(SelectResults r){
		boolean done = relRes.remove(r);
		if(done){
			relIndex--;
			//logger.debug("removeResult(): remove "+r.getNum()+" relIndex"+relIndex);
		}
		return done;
	}
	
	public TreeSet<ResultRegistry> getRelResult(){
		return relRes;
	}
	
	/**
	 * Add new values to this variable, the result is a union of original values and new 
	 * values. If list v is null, it means new values are any allowed data.
	 * @param v
	 * @param n
	 */
	public void addValues(List<T> v){
		if(v != null){
			if(values == null){
				values = new ArrayList<T>();
				for(int i = 0; i < v.size(); i++){
					values.add(v.get(i));
				}
			}
			else{
				List<T> union = new ArrayList<T>();
				for(int i = 0; i < values.size(); i++){
					T t = values.get(i);
					for(int j = 0; j < v.size(); j++){
						if(t.equals(v.get(j))){
							union.add(t);
								logger.debug("addValues(): union "+t);
							break;
						}
					}
				}
				values = union;
			}
		}
		// if v == null, it means there is no value restriction on this variable
	}
}
