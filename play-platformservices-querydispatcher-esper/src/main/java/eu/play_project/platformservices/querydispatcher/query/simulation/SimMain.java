/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;

import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPPreparedStatement;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventType;
import com.espertech.esper.example.transaction.TransactionSamplePlugin;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompiler;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.CoordinateSystemListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLPreparedQuery;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLQuery;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLQuery;
import eu.play_project.platformservices.querydispatcher.query.extension.function.FunctionManager;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FilterFunctionLoadException;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionParameterTypeException;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionTable;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.IFunction;
import eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia.ArrEventSim;
import eu.play_project.platformservices.querydispatcher.query.simulation.coordinateUI.CoordinatePanel;



/**
 * @author ningyuan 
 * 
 * Apr 30, 2014
 *
 */
public class SimMain extends WindowAdapter{
	
	private Object lock = new Object();
	
	private String engineURI = "TestEspEngine";
	private EPServiceProvider epService = null;
	private TransactionSamplePlugin plugin = null;
	private Map<String, Thread> eFeeders;
	
	private static String EVENT_DEFAULT = "default", EVENT_ECG = "ecg";
	
	private int statementCounter = 0;
	private Map<Integer, EPStatementEntry> stmts;
	
	private FunctionManager fManager;
	
	private BDPLCompiler compiler;
	
	private SimMain(){
		stmts = new HashMap<Integer, EPStatementEntry>();
		
		eFeeders = new HashMap<String, Thread>();
		eFeeders.put(EVENT_ECG, null);
		
		fManager = FunctionManager.getInstance();
		try {
			fManager.initiateTable();
		} catch (FunctionParameterTypeException e) {
			System.out.println(e.getMessage());
		}
		
		compiler = new BDPLCompiler();
	}
	
	public void startShell(){
		BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
		
		String cmd;
		StringBuffer bdpl = new StringBuffer();
		
		while(true){
			try{
				System.out.print("> ");
				cmd = bin.readLine().trim();
				
				if(cmd.startsWith("start")){
					String [] as = cmd.split("\\s+");
					
					if(as.length > 1){
						if(as[1].equals("-s")){
							synchronized(lock){
								if(epService != null){
									try{
										int i = Integer.valueOf(as[2]);
										EPStatementEntry entry = stmts.get(i);
										if(entry != null){
											entry.start();
											System.out.println("[Statement "+as[2]+" is started]");
										
										}
										else{
											System.out.println("[No statement with name "+as[2]+"]");
										
										}
									}
									catch(NumberFormatException e){
										System.out.println("[No statement with name "+as[2]+"]");
										continue;
									}
								}
								else{
									System.out.println("[Engine is not started]");
								
								}
							}
						}
						else if(as[1].equals("-e")){
							synchronized(lock){
							if(epService != null){
								String en = as[2];
								if(en.equals(EVENT_DEFAULT)){
									plugin = new TransactionSamplePlugin();
									plugin.postInitialize(engineURI, epService);
									System.out.println("[Event Feeding is started]");
								}
								else if(en.equals(EVENT_ECG)){
									long interval = Long.valueOf(as[3]);
									Thread et = eFeeders.get(en);
									if(et == null){
										et = new Thread(new ArrEventSim(interval, as[4], epService));
										eFeeders.put(en, et);
										et.start();
										System.out.println("[Arr Event Feeding is started]");
									}
									else{
										System.out.println("[Arr Event Feeding is already started]");
									}
								}
								else{
									System.out.println("[Unknown Event Feeding]");
								}
							}
							else{
								System.out.println("[Engine is not started]");
							}
							}
						}
					}
					else{
						synchronized(lock){
							if(epService == null){
								
								
								 // Configure engine with event names to make the statements more readable.
						        // This could also be done in a configuration file.
						        Configuration configuration = new Configuration();
						        //configuration.addPlugInSingleRowFunction("filter1", "eu.play_project.platformservices.querydispatcher.query.compiler.generation.filter.RDFGraphEventFilter", "evaluate");
						        //configuration.addPlugInSingleRowFunction("filter2", "eu.play_project.platformservices.querydispatcher.query.compiler.generation.filter.RealTimeResultBindingFilter", "evaluate");
						        //configuration.addPlugInPatternGuard("bdpl", "andguard", "ningyuan.pan.query.parser.bdpl.guard.AndGuardFactory");
						        //configuration.addPlugInPatternObserver("bdpl", "andobs", "ningyuan.pan.query.parser.bdpl.guard.AndObserverFactory");
						        
						        /*configuration.addEventType("RDFEvent1", Event1.class.getName());
						        configuration.addEventType("RDFEvent2", Event2.class.getName());
						        configuration.addEventType("RDFEvent3", Event3.class.getName());*/
						        
						        epService = EPServiceProviderManager.getProvider(engineURI, configuration);
						        System.out.println("[Engine is started]");
								
							}
							else{
								System.out.println("[Engine is already started]");
							}
						}
					}
				}
				else if (cmd.startsWith("stop")){
					String [] as = cmd.split("\\s+");
					
					if(as.length > 2){
						synchronized(lock){
							if(epService != null){
								
								if(as[1].equals("-s")){
									try{
										int i = Integer.valueOf(as[2]);
										EPStatementEntry entry = stmts.get(i);
										if(entry != null){
											entry.stop();
											System.out.println("[Statement "+as[2]+" is stopped]");
											
										}
										else{
											System.out.println("[No statement with name "+as[2]+"]");
											
										}
									}
									catch(NumberFormatException e){
										System.out.println("[No statement with name "+as[2]+"]");
										continue;
									}
								}
								else if(as[1].equals("-e")){
									String en = as[2];
									if(en.equals(EVENT_DEFAULT)){
										if(plugin != null){
											plugin.destroy();
											plugin = null;
										}
										else{
											System.out.println("[Event Feeding is already stopped]");
										}
									}
									else{
										Thread et = eFeeders.get(en);
										if(et != null){
											et.interrupt();
											eFeeders.put(en, null);
											et.join();
										}
									}
								}
								else{
									System.out.println("[Unknown stop command]");
								}
							}
							else{
								System.out.println("[Engine is not started]");
								
							}
						} 
					}
					else{
						synchronized(lock){
							if(epService != null){
								Set<Integer> sts = stmts.keySet();
								for(Integer st : sts){
									EPStatementEntry en = stmts.get(st);
									en.destroy();
								}
								stmts.clear();
								
								if(plugin != null){
									plugin.destroy();
									plugin = null;
								}
								Set<String> efk = eFeeders.keySet();
								for(String en : efk){
									Thread et = eFeeders.get(en);
									if(et != null){
										et.interrupt();
										eFeeders.put(en, null);
										et.join();
									}
								}
								
								epService.destroy();
								epService = null;
								System.out.println("[Engine is stopped]");
							}
							else{
								System.out.println("[Engine is not started]");
							}
						}
					}
				}
				else if(cmd.equals("deploy")){
					System.out.println("[Please input a BDPL ending with 2 enters]");
					System.out.print("> ");
					
					String query="";
					String line = null;
					int emptyLineCount = 0;
					while ((line = bin.readLine()) != null) {
						if (line.length() > 0) {
							emptyLineCount = 0;
							bdpl.append(line+" ");
						}
						else {
							emptyLineCount++;
						}
						
						if (emptyLineCount == 2) {
							emptyLineCount = 0;
							
							query = bdpl.toString().trim();
							bdpl.delete(0, bdpl.length());
							
							break;
						}
					}
					
					if(query.length() > 0){
					
						try{
							IBDPLQuery bdplQuery = compiler.compile(query, null);
							
							synchronized(lock){
								if(epService != null){
									
									EPPreparedStatement prepared = epService.getEPAdministrator().prepareEPL(((DefaultBDPLPreparedQuery)bdplQuery).getEPL());
									List<Integer> injectParas = ((DefaultBDPLPreparedQuery)bdplQuery).getInjectParams();
									Map<Integer, Object> injectParaMapping = ((DefaultBDPLPreparedQuery)bdplQuery).getInjectParaMapping();
									
									for(int i = 0; i < injectParas.size(); i++){
										prepared.setObject(i+1, injectParaMapping.get(injectParas.get(i)));
									}
									
									//EPStatement testStmt = epService.getEPAdministrator().create(prepared);
									//testStmt.addListener(((DefaultBDPLPreparedQuery)bdplQuery).getListener());
									
									
									
									//XXX coordinate listener
									CoordinateSystemListener cLis = (CoordinateSystemListener)(((DefaultBDPLPreparedQuery)bdplQuery).getListener());
									cLis.setEPServiceProvider(epService);
									
									JFrame frame = new JFrame("Coordinate System "+statementCounter);
									CoordinatePanel panel = new CoordinatePanel();
									frame.add(panel);
									frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
									frame.setSize(1020,620);
							        frame.setResizable(false); 
									frame.setVisible(true);      
								           
								    cLis.setPanel(panel);
								    EPStatement testStmt = epService.getEPAdministrator().create(prepared);
									testStmt.addListener(cLis);
									
									++statementCounter;
									EPStatementEntry eps = new EPStatementEntry(query, testStmt);
									eps.setFrame(frame);
									
									/*EPStatement testStmt = epService.getEPAdministrator().createEPL(((DefaultBDPLQuery)bdplQuery).getEPL());
									testStmt.addListener(((DefaultBDPLQuery)bdplQuery).getListener());*/
									        
									stmts.put(statementCounter, eps);
									System.out.println("\n[Statement "+statementCounter+":\n"+query+"\nis started]");
								}
								else{
									System.out.println("\n[Engine is not started]");
								}
							}
							
						}
						catch(BDPLCompileException e){
							System.out.println("\n["+e.getMessage()+"]");
							continue;	
						}
					}
				}
				else if(cmd.startsWith("rem")){
					String [] as = cmd.split("\\s+");
					
					if(as.length > 1){
						try{
							int i = Integer.valueOf(as[1]);
							EPStatementEntry en = stmts.get(i);
							if(en != null){
								en.destroy();
								stmts.remove(i);
							}
						}
						catch(NumberFormatException e){
							System.out.println("[No statement with name "+as[1]+"]");
							continue;
						}
					}
					else{
						System.out.println("[No statement name]");
					}
				}
				else if(cmd.equals("exit")){
					
					synchronized(lock){
						if(epService != null){
							Set<Integer> sts = stmts.keySet();
							for(Integer st : sts){
								EPStatementEntry en = stmts.get(st);
								en.destroy();
							}
							stmts.clear();
							
							if(plugin != null){
								plugin.destroy();
								plugin = null;
							}
							Set<String> efk = eFeeders.keySet();
							for(String en : efk){
								Thread et = eFeeders.get(en);
								if(et != null){
									et.interrupt();
									eFeeders.put(en, null);
									et.join();
								}
							}
						
							epService.destroy();
							epService = null;
						}
					}
					
					break;
				}
				else if(cmd.startsWith("ls")){
					String [] as = cmd.split("\\s+");
					
					if(as.length > 1){
						if(as[1].equals("-s")){
							
								if(as.length > 2){
									try{
										int i = Integer.valueOf(as[2]);
										EPStatementEntry entry = stmts.get(i);
										if(entry != null){
											System.out.println(i+":\n"+entry.getEpl());
										}
										else{
											System.out.println("[No statement with name "+as[2]+"]");
										}
									}
									catch(NumberFormatException e){
										System.out.println("[No statement with name "+as[2]+"]");
										continue;
									}
								}
								else{
									Set<Integer> keys = stmts.keySet();
									for(Integer key : keys){
										EPStatementEntry entry = stmts.get(key);
										System.out.println(key+":\n"+entry.getEpl());
									}
								}
						}
							
						else if(as[1].equals("-e")){
							if(epService != null){
								EventType etypes[] = epService.getEPAdministrator().getConfiguration().getEventTypes();
								for(int i = 0 ; i < etypes.length; i++){
									System.out.println(etypes[i].getName());
								}
							}
							else{
								System.out.println("[Engine is not started]");
							}
						}
						else if(as[1].equals("-f")){
							String[][] eFunctions = fManager.listFunctions();
							for(int i = 0; i < eFunctions.length; i++){
								System.out.println(eFunctions[i][0]+"    "+eFunctions[i][1]);
							}
						}
					}
					else{
						System.out.println("[:D]");
					}
				}
				else if(cmd.startsWith("load")){
					String [] as = cmd.split("\\s+");
					
					if(as.length > 1){
						try{
							if(as[1].equals("-f")){
								
								if(as.length > 3){
									
									fManager.loadFunction(as[2], as[3]);
										
								}
								else{
									System.out.println("[load -f functionname classname]");
								}
								
							}
						}
						catch(FilterFunctionLoadException e){
							System.out.println("["+e.getMessage()+"]");
						}
					}
					else{
						System.out.println("[load option parameters...]");
					}
				}
				// for test
				else if(cmd.startsWith("average")){
					IFunction ef = FunctionTable.getInstance().getFunction("http://events.event-processing.org/function/average");
					if(ef != null){
						System.out.println(ef.invoke(new Object[]{new String[][][]{{{"1", "1"}}, {{"2", "2"}}}}));
					}
				}
			}
			catch(EPException epx){
				epx.printStackTrace();
				continue;
			}
			catch(Exception e){
				e.printStackTrace();
				break;
			}
		}
		
		if(bin != null){
			try {
				bin.close();
			} catch (IOException e1) {}
		}
		System.exit(0);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimMain sm = new SimMain();
		sm.startShell();
	}
	
	

}
