package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

/**
 * This is a data structure that is used to represent a query result.
 * 
 * @author Ningyuan Pan
 *
 */
public class ResultRegistry implements SelectResults {
	
	// number of row in result
	private int size;
	// variables in select (column names)
	private List<String> variables;
	// result data
	private List<List> result = new ArrayList<List>();
	
	private Logger logger = LoggerFactory.getLogger(ResultRegistry.class);
	
	public ResultRegistry(ResultSetWrapper rw){
		makeResult(rw);
	}
	
	public ResultRegistry(){
	}
	
	public int getSize(){
		return size;
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.dcep.distributedetalis.join.SelectResults#getVariables()
	 */
	@Override
	public List<String> getVariables(){
		return this.variables;
	}
	
	@Override
	public void setVariables(List<String> variables){
		this.variables = variables;
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.dcep.distributedetalis.join.SelectResults#getResult()
	 */
	@Override
	public List<List> getResult(){
		return result;
	}
	
	@Override
	public void setResult(List<List> result){
		this.result = result;
		this.size = result.size();
	}
	
	/*
	 * Translate the ResultSetWrapper into List<List> as results
	 */
	private void makeResult(ResultSetWrapper rw){
		if(rw == null){
			this.variables = new ArrayList<String>(0);
			this.size = 0;
			return;
		}
		this.variables = rw.getResultVars();
		
		//TODO size = 0??
		// result has duplicated entries ???
		//size = rw.getRowNumber();
			//logger.info("size: "+size);
		
		int colNum = this.variables.size();
		this.size = 0;
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
	}
}
