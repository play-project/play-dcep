/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
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
 * Not used. (union matched pattern sparql)
 * 
 * 
 * @author ningyuan 
 * 
 * Jul 31, 2014
 *
 */
public class RealTimeResultBindingListener2 implements UpdateListener {
	
	private final Set<String> realTimeCommonVars;
	
	private final String matchedPatternSparql;
	
	private final List<SubQueryTableEntry> subQueris;
	
	public RealTimeResultBindingListener2(Set<String> realTimeCommonVars, String matchedPatternSparql, List<SubQueryTableEntry> subQueris){
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
					System.out.println("RealTimeResultBindingListener2 MEs: "+i);
				
				EventBean eb = newEvents[i];
				EventType et = eb.getEventType();
				String[] enames =  et.getPropertyNames();
			
				for(String n : enames){
						System.out.println("RealTimeResultBindingListener2 ME: "+n);
					MapEvent<EventModel<Model>> sevent = (MapEvent<EventModel<Model>>)eb.get(n);
					if(sevent != null){
						EventModel<Model> eventModel = sevent.get(MapEvent.EVENT_MODEL);
						Model model = eventModel.getModel();
						if(model != null){
								//System.out.println("RealTimeResultBindingListener2 ME: "+n);
								eventModel.getProperties("http://ningyuan.com/id");
							//con.add(model, context);
							
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
				
					System.out.println("RealTimeResultBindingListener2: "+matchedPatternSparql);
				TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, matchedPatternSparql).evaluate();
						
				List<Map<String, String[]>> r = new ArrayList<Map<String, String[]>>();
						
				while(result.hasNext()){
						System.out.println("RealTimeResultBindingListener2 R: ");
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
				//TODO: historical part
				
			}
			
		}catch (RepositoryException e) {
			System.out.println("RepositoryException "+e.getMessage());
			
		} catch (MalformedQueryException e) {
			System.out.println("MalformedQueryException "+e.getMessage());
			
		} catch (QueryEvaluationException e) {
			System.out.println("QueryEvaluationException "+e.getMessage());
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
