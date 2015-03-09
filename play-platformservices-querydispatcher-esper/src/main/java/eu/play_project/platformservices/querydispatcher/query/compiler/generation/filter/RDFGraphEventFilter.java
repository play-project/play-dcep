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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;


/**
 * The filter class of EPL pattern. This filter class is responsible of
 * checking the content relation of RDF events from a pattern and array
 * filter conditions. Because this filter is not applied to the last event
 * of a pattern, it dose not need to create real-time solution as real-time
 * data of the query.
 * 
 * 
 * 
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class RDFGraphEventFilter {
	
	private final static Logger logger = LoggerFactory.getLogger(RDFGraphEventFilter.class);
	
	/**
	 * The static filter method called when filtering the pattern.
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
					logger.debug("[matched event]: "+i);
					
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
									logger.debug(st.getSubject().stringValue()+" "+st.getPredicate().stringValue()+" "+st.getObject().stringValue());
							}*/
							
						}
					}
				}
				logger.debug("[ASK query] : "+String.format(query, "ASK"));
				ret = con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(query, "ASK")).evaluate();
					
				if(ret && arrayFilters.size() > 0){
					List<Map<String, String[]>> varBindings = null;
					
					for(BDPLArrayFilter arrayFilter : arrayFilters){
						if(!arrayFilter.hasVariable()){
							if(!arrayFilter.evaluate()){
									logger.debug("[array filter]: false");
								return false;
							}
						}
						else{
							if(varBindings == null){
								varBindings = new ArrayList<Map<String, String[]>>();
								
								TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(query, "SELECT *")).evaluate();
								
								while(result.hasNext()){
									logger.debug("[solution]: ");
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
										logger.debug(name+": "+var[0]+"   "+var[1]+"   ");
											
										content.put(name, var);
									}
									logger.debug("\n");
									varBindings.add(content);
								}
							}
							
							// remove solutions which do not pass the array filter
							List<Map<String, String[]>> rVarBindings = new ArrayList<Map<String, String[]>>();
							for(int i = 0; i < varBindings.size(); i++){
								Map<String, String[]> varBinding = varBindings.get(i);
								arrayFilter.setDataObject(varBinding, null);
								
								if(!arrayFilter.evaluate()){
									rVarBindings.add(varBinding);
								}
							}
							for(Map<String, String[]> rVarBinding : rVarBindings){
								varBindings.remove(rVarBinding);
							}
						}
					}
					
					if(varBindings != null){
						if(varBindings.size() > 0){
							logger.debug("[array filter]: true");
							return true;
						}
						else{
							logger.debug("[array filter]: false");
							return false;
						}
					}
					else{
						logger.debug("[array filter]: true");
						return true;
					}
					
				}
				else{
					logger.debug(""+ret);
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
