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

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
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
import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResultBindingData;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeSolutionSequence;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeSolutionSequence.RealTimeSolution;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionInvocationException;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionTable;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.IFunction;


/**
 * @author ningyuan 
 * 
 * Jul 25, 2014
 *
 */
public class RealTimeSolutionSequenceFilter {
	
	static public boolean evaluate(String query,  Map[] events, List<BDPLArrayFilter> arrayFilters, RealTimeResultBindingData rtbData){
		boolean ret = false;
		
		Set<String> realTimeCommonVars = rtbData.getRealTimeCommonVars();
		RealTimeSolutionSequence realTimeResults = rtbData.getResults();
		List<SubQueryTableEntry> dArrayEntries = rtbData.getDynamicArrays();
		
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection con = null;
			
		try {
			repo.initialize();
			
			con = repo.getConnection();
			Resource context = new URIImpl("context://");
			
			for(int i = 0; i < events.length; i++){
					System.out.println("RealTimeSolutionSequenceFilter E: "+i);
				
				MapEvent<EventModel<Model>> event = (MapEvent<EventModel<Model>>)events[i];
				if(event != null){
					EventModel<Model> eventModel = event.get(MapEvent.EVENT_MODEL);
					Model model = eventModel.getModel();
					if(model != null){
							eventModel.getProperties("http://ningyuan.com/id");
						con.add(model, context);
						
						Iterator<Statement> itr = model.iterator();
						while(itr.hasNext()){
							Statement st = itr.next();
							//XXX deep copy statement, so that events are not effected???
							con.add(st, context);
								System.out.println(st.getSubject().toString()+" "+st.getPredicate().toString()+" "+st.getObject().toString());
						}
					}
				}
			}
				
				System.out.println("RealTimeSolutionSequenceFilter : "+String.format(query, "ASK"));
			ret = con.prepareBooleanQuery(QueryLanguage.SPARQL, String.format(query, "ASK")).evaluate();
			
			List<Map<String, String[]>> varBindings = null;
			
			if(ret){
				/*
				 * snapshot of arrays
				 */
				//XXX snapshot content may changed meanwhile
				Map<String, String[][][]> daSnapshot = new HashMap<String, String[][][]>();
				for(SubQueryTableEntry dArrayEntry : dArrayEntries){
					BDPLArray array = dArrayEntry.getArrayEntry().getArray();
					String aName = dArrayEntry.getArrayEntry().getName();
					String [][][] src = array.read();
					String [][][] des = new String[src.length][][];
					System.arraycopy(src, 0, des, 0, src.length);
					daSnapshot.put(aName, des);
				}
				
				
				/*
				 * array filters
				 */
				if(arrayFilters.size() > 0){
					for(BDPLArrayFilter arrayFilter : arrayFilters){
						// array filter dose not have variable
						if(!arrayFilter.hasVariable()){
							if(!arrayFilter.evaluate()){
									System.out.println("RealTimeSolutionSequenceFilter array filter: false");
								return false;
							}
						}
						// array filter has variable
						else{
							// create variable bindings only once
							if(varBindings == null){
								varBindings = new ArrayList<Map<String, String[]>>();
								
								TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(query, "SELECT *")).evaluate();
								
								// for each solution
								while(result.hasNext()){
										System.out.println("RealTimeSolutionSequenceFilter result: ");
									BindingSet bs = result.next();
									
									Map<String, String[]> content = new HashMap<String, String[]>();
									
									// for each variable in a solution
									for(String name : bs.getBindingNames()){
										String[] var = new String[3];
	
										Value v = bs.getBinding(name).getValue();
											
										if(v instanceof Literal){
											var[0] = String.valueOf(BDPLConstants.TYPE_LITERAL);
											var[1] = ((Literal) v).getLabel();
											var[2] = v.toString();
										}
										else if(v instanceof URI){
											var[0] = String.valueOf(BDPLConstants.TYPE_IRI);
											var[1] = v.toString();
										}
										else if(v instanceof BNode){
											var[0] = String.valueOf(BDPLConstants.TYPE_BN);
											var[1] = ((BNode) v).getID();
										}
										else{
											String.valueOf(BDPLConstants.TYPE_UNKNOWN);
											var[1] = v.toString();
										}
											
											System.out.print(name+": "+var[0]+"   "+var[1]+"   "+var[2]+"   ");
											
										content.put(name, var);
									}
										System.out.println();
										
									varBindings.add(content);
								}
							}
							
							for(Map<String, String[]> varBinding : varBindings){
								arrayFilter.setDataObject(varBinding, daSnapshot);
								if(!arrayFilter.evaluate()){
										System.out.println("RealTimeSolutionSequenceFilter array filter: false");
									return false;
								}
							}
						}
					} 
				}
				
			
				/*
				 * construct variables 
				 */
				if(realTimeCommonVars.size() > 0 && varBindings == null){
					
					varBindings = new ArrayList<Map<String, String[]>>();
						
					TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, String.format(query, "SELECT *")).evaluate();
						
					// for each variable binding in SPARQL result
					while(result.hasNext()){
							System.out.println("RealTimeSolutionSequenceFilter result: ");
						BindingSet bs = result.next();
							
						Map<String, String[]> content = new HashMap<String, String[]>();
							
						// for each variable in a variable binding
						for(String name : bs.getBindingNames()){
							String[] var = new String[3];

							Value v = bs.getBinding(name).getValue();
								
							if(v instanceof Literal){
								var[0] = String.valueOf(BDPLConstants.TYPE_LITERAL);
								var[1] = ((Literal) v).getLabel();
								var[2] = v.toString();
							}
							else if(v instanceof URI){
								var[0] = String.valueOf(BDPLConstants.TYPE_IRI);
								var[1] = v.toString();
							}
							else if(v instanceof BNode){
								var[0] = String.valueOf(BDPLConstants.TYPE_BN);
								var[1] = ((BNode) v).getID();
							}
							else{
								String.valueOf(BDPLConstants.TYPE_UNKNOWN);
								var[1] = v.toString();
							}
									
							System.out.print(name+": "+var[0]+"   "+var[1]+"   "+var[2]+"   ");
									
							content.put(name, var);
								
						}
							System.out.println();
						varBindings.add(content);
					}
				}
					
	
				/*
				 * dynamic arrays
				 */
				// for each variable binding in SPARQL result
				for(Map<String, String[]> varBinding : varBindings){
						
					// for each dynamic array
					for(SubQueryTableEntry dArrayEntry : dArrayEntries){
						BDPLArray array = dArrayEntry.getArrayEntry().getArray();
						String toArrayM = dArrayEntry.getToArrayMethod();
						String [] sVars = dArrayEntry.getSelectedVars();
							
						// insert variables
						if(toArrayM == null){
								
							String [][] ele = new String [sVars.length][3];
											
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
											
										// for test
										System.out.println("RealTimeSolutionSequenceFilter write dynamic array: "+array.length());
										for(int n = 0; n < ele.length; n++){
											System.out.print(sVars[n]+": "+ele[n][0]+"   "+ele[n][1]+"   "+ele[n][2]+"   ");
										}
										System.out.println();
												
								} catch (BDPLArrayException e) {}
							}
						}
						// parse array string
						else {
							FunctionTable eft = FunctionTable.getInstance();
								
							String[] value = varBinding.get(sVars[0]);
								
							if(value == null || value[1].isEmpty()){
								continue;
							}
							else{
									
								IFunction ef = eft.getFunction(toArrayM);
								String [][][] eles = null;
								if(ef != null){
									try {
										eles = (String[][][])ef.invoke(value[1]);
										if(eles != null){
											array.write(eles);
												System.out.println("RealTimeSolutionSequenceFilter write dynamic array: "+array.length());
										}
									} catch (FunctionInvocationException e) {
										e.printStackTrace();
										continue;
									} catch (BDPLArrayException e) {}
								}
							}
						}
					}
				}
					
					
				/*
				 * real time solution sequence
				 */
				RealTimeSolution rts = new RealTimeSolution(varBindings, daSnapshot);
				realTimeResults.put(rts);
					
					System.out.println("RealTimeSolutionSequenceFilter: "+ret);
				return ret;
			}
			// ask query failed
			else{
					System.out.println("RealTimeSolutionSequenceFilter: "+ret);
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
