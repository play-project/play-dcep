package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

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
		
	public ResultRegistry(){
	}
	
	@Override
	public int getSize(){
		return size;
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.dcep.tests.distributedetalis.join.SelectResults#getVariables()
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
	 * @see eu.play_project.dcep.tests.distributedetalis.join.SelectResults#getResult()
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
	
	/**
	 * Create a {@link ResultRegistry} from a Jena {@link ResultSet}.
	 */
	public static ResultRegistry makeResult(ResultSet rs){
		final Logger logger = LoggerFactory.getLogger(ResultRegistry.class);

		ResultRegistry result = new ResultRegistry();
		if(rs == null){
			result.variables = new ArrayList<String>(0);
			result.size = 0;
			return result;
		}
		
		result.variables = rs.getResultVars();
		
		//TODO size = 0??
		// result has duplicated entries ???
		//size = rw.getRowNumber();
			//logger.info("size: {}", size);
		
		int colNum = result.variables.size();
		result.size = 0;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			List<String> data = new ArrayList<String>(colNum);
			for(int i = 0; i < colNum; i++){
				data.add(qs.get(result.variables.get(i)).toString());
			}
			result.getResult().add(data);
			result.size++;
		}
		logger.debug("Found {} results for historical query.", result.size);
		return result;
	}
	
	
	/**
	 * Create a {@link ResultRegistry} from a RDF2Go {@link QueryResultTable}.
	 */
	public static ResultRegistry makeResult(QueryResultTable queryResult){
		final Logger logger = LoggerFactory.getLogger(ResultRegistry.class);

		ResultRegistry result = new ResultRegistry();
		
		result.variables = queryResult.getVariables();
				
		int colNum = result.variables.size();
		result.size = 0;
		
		ClosableIterator<QueryRow> it = queryResult.iterator();
		
		while(it.hasNext()){
			QueryRow qr = it.next();
			List<String> data = new ArrayList<String>(colNum);
			for (String var : result.variables) {
				data.add(qr.getValue(var).toString());
			}
			result.getResult().add(data);
			result.size++;
		}
		logger.debug("Found {} results for historical query.", result.size);
		return result;
	}

	
}
