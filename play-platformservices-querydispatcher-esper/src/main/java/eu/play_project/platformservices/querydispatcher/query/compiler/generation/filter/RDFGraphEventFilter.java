/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.filter;


import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;


/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class RDFGraphEventFilter {
		
		static public boolean evaluate(String query,  Map[] events){
			boolean ret = false;
			
			Repository repo = new SailRepository(new MemoryStore());
			RepositoryConnection con = null;
				
			try {
				repo.initialize();
				
				con = repo.getConnection();
				Resource context = new URIImpl("context://");
				
				for(int i = 0; i < events.length; i++){
						System.out.println("RDFGraphEventFilter E: "+i);
					
					MapEvent<EventModel<Model>> event = (MapEvent<EventModel<Model>>)events[i];
					if(event != null){
						EventModel<Model> eventModel = event.get(MapEvent.EVENT_MODEL);
							
						Model model = eventModel.getModel();
						if(model != null){
								
							//con.add(model, context);
								
								eventModel.getProperties("http://ningyuan.com/id");
							Iterator<Statement> itr = model.iterator();
							while(itr.hasNext()){
								Statement st = itr.next();
								//XXX deep copy statement, so that events are not effected???
								con.add(st, context);
									System.out.println(st.getSubject().stringValue()+" "+st.getPredicate().stringValue()+" "+st.getObject().stringValue());
							}
							
						}
					}
				}
					System.out.println("RDFGraphEventFilter : "+String.format(query, "ASK"));
				ret = con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(query, "ASK")).evaluate();
					System.out.println("RDFGraphEventFilter: "+ret);
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
