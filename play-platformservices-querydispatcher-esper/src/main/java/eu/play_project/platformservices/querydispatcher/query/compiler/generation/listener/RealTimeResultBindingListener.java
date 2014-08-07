/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTableEntry;
import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;


/**
 * Not used (multiple matched pattern sparql)
 * 
 * @author ningyuan 
 * 
 * Jul 24, 2014
 *
 */
public class RealTimeResultBindingListener implements UpdateListener{
	
	private final Set<String> realTimeCommonVars;
	
	private final List<String> matchedPatternSparql;
	
	private final List<SubQueryTableEntry> subQueris;
	
	public RealTimeResultBindingListener(Set<String> realTimeCommonVars, List<String> matchedPatternSparql, List<SubQueryTableEntry> subQueris){
		this.realTimeCommonVars = realTimeCommonVars;
		this.matchedPatternSparql = matchedPatternSparql;
		this.subQueris = subQueris;
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
				
				EventBean eb = newEvents[i];
				EventType et = eb.getEventType();
				String[] enames =  et.getPropertyNames();
			
				for(String n : enames){
					
					MapEvent<EventModel<Model>> sevent = (MapEvent<EventModel<Model>>)eb.get(n);
					if(sevent != null){
						EventModel<Model> eventModel = sevent.get(MapEvent.EVENT_MODEL);
						Model model = eventModel.getModel();
						if(model != null){
								System.out.println("RealTimeResultBindingListener ME: "+n);
							con.add(model, context);
						
						}
					}
				}
				
				for(String ms : matchedPatternSparql){
					
					if(con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(ms, "ASK")).evaluate()){
					
						TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(ms, "SELECT *")).evaluate();
						
						List<Map<String, String[]>> r = new ArrayList<Map<String, String[]>>();
						
						while(result.hasNext()){
							BindingSet bs = result.next();
							Map<String, String[]> m = new HashMap<String, String[]>();
							r.add(m);
							for(String name : realTimeCommonVars){
								String[] var = new String[2];
								
								Value v = bs.getBinding(name).getValue();
								
								if(v instanceof Literal){
									var[0] = ((Literal) v).getLabel();
								}
								else{
									var[0] = v.toString();
								}
								
								var[1] = bs.getBinding(name).getValue().toString();
								
								m.put(name, var);
							}
						}
						
						
						//TODO: handel variables
						for(SubQueryTableEntry subQuery : subQueris){
							BDPLArray array = subQuery.getArray();
							String [] sVars = subQuery.getSelectedVars();
							
							for(Map<String, String[]> m : r){
								String [][] ele = new String [sVars.length][2];
								
								int k = 0;
								for( ; k < sVars.length; k++){
									String sVar = sVars[k];
									String[] value = m.get(sVar);
									
									if(value == null || value[1].isEmpty()){
										break;
									}
									else{
										ele[k] = value;
									}
								}
								
								if(k == sVars.length){
									try {
											
										array.write(ele);
											System.out.println("Add element in dynamic array: "+array.length());
											for(int n = 0; n < ele.length; n++){
												System.out.print(sVars[n]+": "+ele[n][0]+"   "+ele[n][1]+"   ");
											}
											System.out.println();
									} catch (BDPLArrayException e) {}
								}
							}
						}
						
						con.clear(context);
						break;
					}
				}
				//TODO: historical part
				
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
