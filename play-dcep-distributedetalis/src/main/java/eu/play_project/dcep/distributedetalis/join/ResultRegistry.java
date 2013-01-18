/**
 * 
 */
package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

/**
 * @author Ningyuan Pan
 *
 */
public class ResultRegistry {
	//for test
	private int num;
	
	// number of row in result
	private int size;
	// variables in select
	private List<String> variables;
	private List<List> result = new ArrayList<List>();
	
	private Logger logger = LoggerFactory.getLogger(ResultRegistry.class);
	
	//for test
	public ResultRegistry(int i, List<String> v){
		variables = v;
		num = i;
	}
	
	public ResultRegistry(int i, ResultSetWrapper rw){
		makeResult(rw);
		num = i;
	}
	
	public ResultRegistry(ResultSetWrapper rw){
		makeResult(rw);
	}
	
	public int getSize(){
		return size;
	}
	
	public List<String> getVariables(){
		return variables;
	}
	
	public List<List> getResult(){
		return result;
	}
	
	// for test
	public int getNum(){
		return num;
	}
	// for test
	public void setSize(int i){
		size = i;
	}
	// for test	
	public void setResult(List<List> r){
		result = r;
		size = r.size();
	}
	
	/*
	 * Translate the ResultSetWrapper into List<List> as results
	 */
	private void makeResult(ResultSetWrapper rw){
		if(rw == null){
			variables = new ArrayList<String>(0);
			size = 0;
			return;
		}
		
		System.out.println("Result from Event cloud:");
		variables = rw.getResultVars();
		if(variables != null){
			for(int i = 0; i < variables.size(); i++){
				System.out.print(variables.get(i)+" ");
			}
			System.out.println();
		}
		
		//TODO size = 0??
		// result has duplicated entries ???
		//size = rw.getRowNumber();
			//logger.info("size: "+size);
		
		int colNum = variables.size();
		size = 0;
		QuerySolution qs;
		while(rw.hasNext()){
			qs = rw.next();
			List<String> data = new ArrayList<String>(colNum);
			for(int i = 0; i < colNum; i++){
					//logger.debug("add: "+qs.get(variables.get(i)).toString());
				data.add(qs.get(variables.get(i)).toString());
			}
			result.add(data);
			size++;
		}
		
		//for test
		System.out.println();
		for(int i=0; i < result.size(); i++){
			List<String> ls = result.get(i);
			for(int j=0; j < ls.size(); j++){
				System.out.print(ls.get(j)+" ");
			}
			System.out.println();
		}
	}
}
