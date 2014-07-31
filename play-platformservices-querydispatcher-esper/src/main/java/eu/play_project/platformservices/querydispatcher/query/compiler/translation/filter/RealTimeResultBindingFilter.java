/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.filter;

import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameMapEvent;

/**
 * @author ningyuan 
 * 
 * Jul 25, 2014
 *
 */
public class RealTimeResultBindingFilter {
	
	static public boolean evaluate(String query,  Map[] events){
		boolean ret = false;
		
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection con = null;
			
		try {
			repo.initialize();
			
			con = repo.getConnection();
			Resource context = new URIImpl("context://");
			
			for(int i = 0; i < events.length; i++){
					System.out.println("RealTimeResultBindingFilter E: "+i);
				
				SesameMapEvent sevent = (SesameMapEvent)events[i];
				if(sevent != null){
					SesameEventModel eventModel = sevent.get(MapEvent.EVENT_MODEL);
					Model model = eventModel.getModel();
					if(model != null){
						con.add(model, context);
						/*Iterator<Statement> itr = model.iterator();
						while(itr.hasNext()){
							Statement st = itr.next();
							//XXX deep copy statement, so that events are not effected???
							con.add(st, new Resource [0]);
								System.out.println("A: "+st.getSubject().stringValue()+" "+st.getPredicate().stringValue()+" "+st.getObject().stringValue());
						}*/
						
					}
				}
			}
			
			ret = con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(query, "ASK")).evaluate();
			if(ret){
				TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(query, "SELECT *")).evaluate();
				//TODO
			}
				
				System.out.println(Thread.currentThread().getName()+"   RealTimeResultBindingFilter: "+ret);
			return ret;
			
			
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
