/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.sparql.repository;


import java.io.IOException;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.memory.MemoryStore;

import eu.play_project.platformservices.querydispatcher.query.sparql.SparqlRepository;

/**
 * @author ningyuan 
 * 
 * Jul 2, 2014
 *
 */
public class TestRepository implements SparqlRepository{
	
	private Repository repo;
	
	@Override
	public Object query(String query) {
		RepositoryConnection con = null;
		Object ret = null;
		try {
			con = repo.getConnection();
			
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			
			ret = tupleQuery.evaluate();
			
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			
			e.printStackTrace();
		}
		finally{
			try {
				if(con != null)
					con.close();
				
			} catch (RepositoryException e) {
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
		Model model = null;
		try {
			
			model = Rio.parse(TestRepository.class.getResourceAsStream("/java/rdf/ScenarioIntelligentTransportTest_Users.trig"), "", RDFFormat.TRIG, new Resource [0]);
			
		} catch (RDFParseException e1) {
			e1.printStackTrace();
		} catch (UnsupportedRDFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
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
			
			con.add(model, new Resource [0]);
			
		} catch (RepositoryException e) {
			e.printStackTrace();
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
