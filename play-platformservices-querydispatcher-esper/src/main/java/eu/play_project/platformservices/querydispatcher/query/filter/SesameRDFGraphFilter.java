/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.filter;


import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.eventImpl.rdf.sesame.SesameEventModel;

/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class SesameRDFGraphFilter {
	
		static public boolean evaluate(String sparqlQuery,  MapEvent<SesameEventModel>... events){
		
			Repository repo = new SailRepository(new MemoryStore());
			RepositoryConnection con = null;
			
			try {
				repo.initialize();
				
				con = repo.getConnection();
				
				for(MapEvent<SesameEventModel> event : events){
					SesameEventModel eventModel = event.get(MapEvent.EVENT_MODEL);
					Model model = eventModel.getModel();
					if(model != null){
						con.add(model, new Resource [0]);
					}
				}
				
				return con.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate();
				
				
			} catch (RepositoryException e) {
				e.printStackTrace();
				return false;
			} catch (MalformedQueryException e) {
				e.printStackTrace();
				return false;
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
				return false;
			}
			finally{
				try {
					if(con != null)
						con.close();
					repo.shutDown();
					
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				
			}
	}
}
