/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.filter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;


/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class RDFGraphEventFilter {
	
	/**
	 * 
	 * @param query (must not be null)
	 * @param events (must not be null)
	 * @param arrayFilters (must not be null)
	 * @return
	 */
	static public boolean evaluate(String query,  Map[] events, List<BDPLArrayFilter> arrayFilters){
			boolean ret = false;
			
			Repository repo = new SailRepository(new MemoryStore());
			RepositoryConnection con = null;
				
			try {
				repo.initialize();
				
				con = repo.getConnection();
				Resource context = new URIImpl("context://");
				
				for(int i = 0; i < events.length; i++){
						System.out.println("RDFGraphEventFilter matched event: "+i);
					
					MapEvent<EventModel<Model>> event = (MapEvent<EventModel<Model>>)events[i];
					if(event != null){
						EventModel<Model> eventModel = event.get(MapEvent.EVENT_MODEL);
							
						Model model = eventModel.getModel();
						if(model != null){
								eventModel.getProperties("http://ningyuan.com/id");
							con.add(model, context);
								
							/*Iterator<Statement> itr = model.iterator();
							while(itr.hasNext()){
								Statement st = itr.next();
								//XXX deep copy statement, so that events are not effected???
								con.add(st, context);
									System.out.println(st.getSubject().stringValue()+" "+st.getPredicate().stringValue()+" "+st.getObject().stringValue());
							}*/
							
						}
					}
				}
					System.out.println("RDFGraphEventFilter : "+String.format(query, "ASK"));
				ret = con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(query, "ASK")).evaluate();
					
				if(ret && arrayFilters.size() > 0){
					List<Map<String, String[]>> varBindings = null;
					
					for(BDPLArrayFilter arrayFilter : arrayFilters){
						if(!arrayFilter.hasVariable()){
							if(!arrayFilter.evaluate()){
									System.out.println("RDFGraphEventFilter array filter: false");
								return false;
							}
						}
						else{
							if(varBindings == null){
								varBindings = new ArrayList<Map<String, String[]>>();
								
								TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(query, "SELECT *")).evaluate();
								
								while(result.hasNext()){
										System.out.println("RDFGraphEventFilter var binding: ");
									BindingSet bs = result.next();
									
									Map<String, String[]> content = new HashMap<String, String[]>();
									
									for(String name : bs.getBindingNames()){
										String[] var = new String[2];

										Value v = bs.getBinding(name).getValue();
											
										if(v instanceof Literal){
											var[0] = ((Literal) v).getLabel();
										}
										else{
											var[0] = v.toString();
										}
											
										var[1] = v.toString();
											System.out.print(name+": "+var[0]+"   "+var[1]+"   ");
											
										content.put(name, var);
									}
										System.out.println();
									varBindings.add(content);
								}
							}
							
							for(Map<String, String[]> varBinding : varBindings){
								arrayFilter.setDataObject(varBinding);
								if(!arrayFilter.evaluate()){
										System.out.println("RDFGraphEventFilter array filter: false");
									return false;
								}
							}
						}
					} 
						System.out.println("RDFGraphEventFilter array filter: "+ret);
					return ret;
				}
				else{
						System.out.println("RDFGraphEventFilter: "+ret);
					return ret;
				}
				
				
			} catch (RepositoryException e) {
				e.printStackTrace();
				return false;
			} catch (MalformedQueryException e) {
				e.printStackTrace();
				return false;
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
				return false;
			} catch (BDPLFilterException e) {
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
