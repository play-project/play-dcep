/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.sparql.repository;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import eu.play_project.platformservices.querydispatcher.query.sparql.ISparqlRepository;

/**
 * @author ningyuan 
 * 
 * Jul 2, 2014
 *
 */
public class TestRepository implements ISparqlRepository{
	
	private Repository repo;
	
	/**
	 * 
	 * @return return null if exceptions are thrown out
	 * 
	 * (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.sparql.ISparqlRepository#query(java.lang.String)
	 */
	@Override
	public String[][][] query(String query) {
		RepositoryConnection con = null;
		TupleQueryResult result = null;
		String[][][] ret = null;
		
		try {
			con = repo.getConnection();
			
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			
			result = tupleQuery.evaluate();
			
			/*
			 * the sequence of selected variables is kept 
			 */
			List<String> names = result.getBindingNames();
				for(int i = 0; i < names.size(); i++){
					System.out.println("names: "+names.get(i));
				}
			List<String[][]> temp = new ArrayList<String[][]>();
			
			while(result.hasNext()){
				BindingSet bindingSet = result.next();
				
				String [][] ele = new String[names.size()][2];
				temp.add(ele);
				for(int i = 0; i < names.size(); i++){
					Value v = bindingSet.getBinding(names.get(i)).getValue();
					
					if(v instanceof Literal){
						ele[i][0] = ((Literal) v).getLabel();
					}
					else{
						ele[i][0] = v.toString();
					}
					
					ele[i][1] = v.toString();
				}
				
			}
			
			ret = new String[temp.size()][names.size()][2];
			for(int i = 0; i < temp.size(); i++){
				ret[i] = temp.get(i);
					String [][] s = ret[i];
					for(int j = 0; j < s.length; j++){
						System.out.print(s[j][0]+"   "+s[j][1]+"   ");
					}
					System.out.println();
			}
			
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			
			e.printStackTrace();
		}
		finally{
			try {
				if(result != null){
					result.close();
				}
				if(con != null){
					con.close();
				}
				
			} catch (RepositoryException | QueryEvaluationException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}

	@Override
	public void close() {
		if(repo != null){
			try{
				repo.shutDown();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start() {
		
		repo = new SailRepository( new MemoryStore() );
		
		try {
			repo.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
			try {
				repo.shutDown();
				repo = null;
			} catch (RepositoryException re) {
				re.printStackTrace();
			}
		}
		
		RepositoryConnection con = null;
		try {
			con = repo.getConnection();
			con.add(TestRepository.class.getResourceAsStream("/rdf/test_data.trig"), "", RDFFormat.TRIG, new Resource[0]);
			
		} catch(RDFParseException re){
			re.printStackTrace();
		} 
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		} 
		finally{
			try {
				if(con != null)
					con.close();
				
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}

}
