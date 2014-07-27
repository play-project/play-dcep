/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation;


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

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameMapEvent;

/**
 * @author ningyuan 
 * 
 * Jul 24, 2014
 *
 */
public class RealTimeResultBindingListener implements UpdateListener{
	
	private final String [] queries;
	
	public RealTimeResultBindingListener(String[] queries){
		
		this.queries = queries;
	}
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection con = null;
			
		try {
			repo.initialize();
			Resource context = new URIImpl("context://");
			con = repo.getConnection();
			
			for(int i = 0; i < newEvents.length; i++){
				
				con.clear(context);
			
				EventBean eb = newEvents[i];
				EventType et = eb.getEventType();
				String[] enames =  et.getPropertyNames();
			
				for(String n : enames){
					System.out.print(n+":   ");
					SesameMapEvent sevent = (SesameMapEvent)eb.get(n);
					SesameEventModel eventModel = sevent.get(MapEvent.EVENT_MODEL);
					Model model = eventModel.getModel();
					if(model != null){
						con.add(model, context);
					}
				}
				
				for(int j = 0; j < queries.length; j++){
					
					if(con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(queries[j], " ASK ")).evaluate()){
						TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(queries[j], " SELECT * ")).evaluate();
						
						break;
					}
				}
				//TODO
			}
			
		}catch (RepositoryException e) {
			e.printStackTrace();
			//TODO
			
		} catch (MalformedQueryException e) {
			e.printStackTrace();
			//TODO
			
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
			//TODO
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
