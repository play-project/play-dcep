/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.filter;


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

import eu.play_project.platformservices.bdpl.parser.util.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResultBindingData;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResults;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;


/**
 * @author ningyuan 
 * 
 * Jul 25, 2014
 *
 */
public class RealTimeResultBindingFilter {
	
	static public boolean evaluate(String query,  Map[] events, List<BDPLArrayFilter> arrayFilters, RealTimeResultBindingData rtbData){
		boolean ret = false;
		
		Set<String> realTimeCommonVars = rtbData.getRealTimeCommonVars();
		RealTimeResults realTimeResults = rtbData.getResults();
		List<SubQueryTableEntry> dArrayEntries = rtbData.getDynamicArrays();
		
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection con = null;
			
		try {
			repo.initialize();
			
			con = repo.getConnection();
			Resource context = new URIImpl("context://");
			
			for(int i = 0; i < events.length; i++){
					System.out.println("RealTimeResultBindingFilter E: "+i);
				
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
				
				System.out.println("RealTimeResultBindingFilter : "+String.format(query, "ASK"));
			ret = con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(query, "ASK")).evaluate();
			
			List<Map<String, String[]>> varBindings = null;
			
			// has array filters
			if(ret && arrayFilters.size() > 0){
				
				for(BDPLArrayFilter arrayFilter : arrayFilters){
					// array filter dose not have variable
					if(!arrayFilter.hasVariable()){
						if(!arrayFilter.evaluate()){
								System.out.println("RealTimeResultBindingFilter array filter: false");
							return false;
						}
					}
					// array filter has variable
					else{
						if(varBindings == null){
							varBindings = new ArrayList<Map<String, String[]>>();
							
							TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(query, "SELECT *")).evaluate();
							
							while(result.hasNext()){
									System.out.println("RealTimeResultBindingFilter result: ");
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
									System.out.println("RealTimeResultBindingFilter array filter: false");
								return false;
							}
						}
						
					}
				} 
					System.out.println("RealTimeResultBindingFilter array filter: "+ret);
				return ret;
			}
			// dose not have array filter
			else if(ret){
				// has constructor variables
				if(realTimeCommonVars.size() > 0){
					
					if(varBindings == null){
						varBindings = new ArrayList<Map<String, String[]>>();
						
						TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(query, "SELECT *")).evaluate();
						
						while(result.hasNext()){
								System.out.println("RealTimeResultBindingFilter result: ");
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
					
					
					/*
					 * real time result
					 */
					realTimeResults.put(varBindings);
					
					/*
					 * dynamic arrays
					 */
					for(Map<String, String[]> varBinding : varBindings){
						
						for(SubQueryTableEntry dArrayEntrie : dArrayEntries){
							BDPLArray array = dArrayEntrie.getArray();
							String [] sVars = dArrayEntrie.getSelectedVars();
									
							
							String [][] ele = new String [sVars.length][2];
										
							int k = 0;
							for( ; k < sVars.length; k++){
								String sVar = sVars[k];
								String[] value = varBinding.get(sVar);
											
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
										
										System.out.println("RealTimeResultBindingFilter dynamic array: "+array.length());
										for(int n = 0; n < ele.length; n++){
											System.out.print(sVars[n]+": "+ele[n][1]+"   "+ele[n][0]+"   ");
										}
										System.out.println();
											
								} catch (BDPLArrayException e) {}
							}
						}
					}
						System.out.println("RealTimeResultBindingFilter: "+ret);
					return ret;
				}
				else{
						System.out.println("RealTimeResultBindingFilter: "+ret);
					return ret;
				}
			}
			// ask query failed
			else{
					System.out.println("RealTimeResultBindingFilter: "+ret);
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
