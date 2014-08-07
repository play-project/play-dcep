/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPPreparedStatement;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventType;
import com.espertech.esper.example.transaction.TransactionSamplePlugin;


import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompiler;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLPreparedQuery;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLQuery;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLQuery;
import eu.play_project.platformservices.querydispatcher.query.extension.function.ExFunctionSignature;
import eu.play_project.platformservices.querydispatcher.query.extension.function.ExFunctionClassLoader;
import eu.play_project.platformservices.querydispatcher.query.extension.function.ExFunctionInitiator;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.ExternalFunctionTable;


/**
 * @author ningyuan 
 * 
 * Apr 30, 2014
 *
 */
public class SimMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Object lock = new Object();
		
		TransactionSamplePlugin plugin = null;
		EPServiceProvider epService = null;
		
		int statementCounter = 0;
		Map<Integer, EPStatementEntry> stmts = new HashMap<Integer, EPStatementEntry>();
		
		ExternalFunctionTable fTable = ExternalFunctionTable.getInstance();
		ExFunctionInitiator.initiate(fTable);
		
		ExFunctionClassLoader fLoader = null;
		try {
			fLoader = new ExFunctionClassLoader("D:/Neo/Materials/Codes/Java/EclipseWorkspace/play-dcep/play-platformservices-querydispatcher-esper/exFunctionLib");
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		}
		
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
						synchronized(lock){
							if(epService != null){
								try{
									int i = Integer.valueOf(as[1]);
									EPStatementEntry entry = stmts.get(i);
									if(entry != null){
										entry.getStatement().start();
										System.out.println("[Statement "+as[1]+" is started]");
										
									}
									else{
										System.out.println("[No statement with name "+as[1]+"]");
										
									}
								}
								catch(NumberFormatException e){
									System.out.println("[No statement with name "+as[1]+"]");
									continue;
								}
							}
							else{
								System.out.println("[Engine is not started]");
								
							}
						}
					}
					else{
						synchronized(lock){
							if(epService == null){
								String engineURI = "TestEspEngine";
								
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
								plugin = new TransactionSamplePlugin();
								plugin.postInitialize(engineURI, epService);
								System.out.println("[Event Feeding is started]");
							}
							else{
								System.out.println("[Engine is already started]");
							}
						}
					}
				}
				else if (cmd.startsWith("stop")){
					String [] as = cmd.split("\\s+");
					
					if(as.length > 1){
						synchronized(lock){
							if(epService != null){
								try{
									int i = Integer.valueOf(as[1]);
									EPStatementEntry entry = stmts.get(i);
									if(entry != null){
										entry.getStatement().stop();
										System.out.println("[Statement "+as[1]+" is stopped]");
										
									}
									else{
										System.out.println("[No statement with name "+as[1]+"]");
										
									}
								}
								catch(NumberFormatException e){
									System.out.println("[No statement with name "+as[1]+"]");
									continue;
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
								stmts.clear();
								plugin.destroy();
								plugin = null;
								epService = null;
								System.out.println("[Engine is stopped]");
							}
							else{
								System.out.println("[Engine is not started]");
							}
						}
					}
				}
				else if(cmd.equals("add")){
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
					
						/*try {
							ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(query);
							StringEscapesProcessor.process(qc);
							BaseDeclProcessor.process(qc, null);
							Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
							WildcardProjectionProcessor.process(qc);
							BlankNodeVarProcessor.process(qc);
	
							if (qc.containsQuery()) {
	
								// handle query operation
								TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(new ValueFactoryImpl());
								TupleExpr tupleExpr = (TupleExpr)qc.jjtAccept(tupleExprBuilder, null);
								
								ParsedQuery pQuery;
							
								ASTQuery queryNode = qc.getQuery();
								if (queryNode instanceof ASTSelectQuery) {
										
									pQuery = new ParsedTupleQuery(query, tupleExpr);
								}
								else if (queryNode instanceof ASTConstructQuery) {
									
									pQuery = new ParsedGraphQuery(query, tupleExpr, prefixes);
									
									String prolog = BDPLSyntaxCheckProcessor.process(qc);
									
									query = EPLTranslationProcessor.process(qc, prolog).getEpl();
									//EPLListenerProcessor.process(qc);
									System.out.println(query+"\n");
								}
								else if (queryNode instanceof ASTAskQuery) {
									
									pQuery = new ParsedBooleanQuery(query, tupleExpr);
								}
								else if (queryNode instanceof ASTDescribeQuery) {
									
									pQuery = new ParsedGraphQuery(query, tupleExpr, prefixes);
								}
								else {
									System.out.println("\n[Unexpected query type: " + queryNode.getClass()+"]");
									continue;
								}
	
								// Handle dataset declaration
								Dataset dataset = DatasetDeclProcessor.process(qc);
								if (dataset != null) {
									pQuery.setDataset(dataset);
								}
								
							}
							else {
								System.out.println("\n[Supplied string is not a query operation]");
							}
						}
						catch (ParseException e){
							System.out.println("\n["+e.getMessage()+"]");
							continue;	
						}
						catch (VisitorException e) {
							System.out.println("\n["+e.getMessage()+"]");
							continue;
						}
						catch (MalformedQueryException e) {
							System.out.println("\n["+e.getMessage()+"]");
							continue;
						}*/
						
						
						try{
							IBDPLQuery bdplQuery = BDPLCompiler.compile(query, null);
							
							synchronized(lock){
								if(epService != null){
									
									EPPreparedStatement prepared = epService.getEPAdministrator().prepareEPL(((DefaultBDPLPreparedQuery)bdplQuery).getEPL());
									List<Integer> injectParas = ((DefaultBDPLPreparedQuery)bdplQuery).getInjectParams();
									Map<Integer, Object> injectParaMapping = ((DefaultBDPLPreparedQuery)bdplQuery).getInjectParaMapping();
									
									for(int i = 0; i < injectParas.size(); i++){
										prepared.setObject(i+1, injectParaMapping.get(injectParas.get(i)));
									}
									
									EPStatement testStmt = epService.getEPAdministrator().create(prepared);
									testStmt.addListener(((DefaultBDPLPreparedQuery)bdplQuery).getListener());
									
									/*EPStatement testStmt = epService.getEPAdministrator().createEPL(((DefaultBDPLQuery)bdplQuery).getEPL());
									testStmt.addListener(((DefaultBDPLQuery)bdplQuery).getListener());*/
									        
									stmts.put(++statementCounter, new EPStatementEntry(query, testStmt));
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
							stmts.remove(i);
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
							stmts.clear();
							plugin.destroy();
							plugin = null;
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
							String[][] eFunctions = fTable.list();
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
								if(fLoader != null){
									if(as.length > 2){
										
										Class fc = fLoader.loadClass(as[2]);
										
										Method[] ms = fc.getDeclaredMethods();
										
										for(int i = 0; i < ms.length; i++){
											ExFunctionSignature ef = new ExFunctionSignature(ms[i].getName(), ms[i].getParameterTypes()); 
											fTable.putFunctionClass(ef, fc);
										}
										
									}
									else{
										System.out.println("[load -f classname]");
									}
								}
								else{
									System.out.println("[Class loader is not initiated properly.]");
								}
							}
						}
						catch(ClassNotFoundException e){
							System.out.println("["+e.getMessage()+"]");
						}
						catch(SecurityException e){
							System.out.println("["+e.getMessage()+"]");
						}
					}
					else{
						System.out.println("[load -f classname]");
					}
				}
				else if(cmd.startsWith("average")){
					Class fc = fTable.getFunctionClass(new ExFunctionSignature("average", new Class[]{double[].class}));
					
					Method m = fc.getMethod("average", double[].class);
					if(m != null){
						m.invoke(fc.newInstance(), new double[]{1,2});
					}
				}
			}
			catch(EPException epx){
				epx.printStackTrace();
				continue;
			}
			catch(Exception e){
				e.printStackTrace();
				System.exit(0);
			}
		}

	}

}
