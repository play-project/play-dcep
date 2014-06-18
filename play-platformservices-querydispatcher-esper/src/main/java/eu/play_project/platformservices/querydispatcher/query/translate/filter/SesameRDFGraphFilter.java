/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate.filter;


import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
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
import eu.play_project.platformservices.querydispatcher.query.eventImpl.rdf.sesame.SesameMapEvent;

/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class SesameRDFGraphFilter {
		
		static public boolean evaluate(String askQuery,  Map[] events){
		
			Repository repo = new SailRepository(new MemoryStore());
			RepositoryConnection con = null;
				//System.out.println(askQuery);
			try {
				repo.initialize();
				
				con = repo.getConnection();
				
				for(int i = 0; i < events.length; i++){
						System.out.println("E: "+i);
					Map event = events[i];
					SesameMapEvent sevent = (SesameMapEvent)event;
					SesameEventModel eventModel = sevent.get(MapEvent.EVENT_MODEL);
					Model model = eventModel.getModel();
					if(model != null){
						con.add(model, new Resource [0]);
						/*Iterator<Statement> itr = model.iterator();
						while(itr.hasNext()){
							Statement st = itr.next();
							//XXX deep copy statement, so that events are not effected???
							con.add(st, new Resource [0]);
								System.out.println("A: "+st.getSubject().stringValue()+" "+st.getPredicate().stringValue()+" "+st.getObject().stringValue());
						}*/
						
					}
				}
				
				boolean ret = con.prepareBooleanQuery(QueryLanguage.SPARQL, askQuery).evaluate();
					System.out.println(ret);
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
