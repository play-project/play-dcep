/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.DatasetDeclProcessor;
import org.openrdf.query.parser.bdpl.PrefixDeclProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.TupleExprBuilder;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTAskQuery;
import org.openrdf.query.parser.bdpl.ast.ASTConstructQuery;
import org.openrdf.query.parser.bdpl.ast.ASTDescribeQuery;
import org.openrdf.query.parser.bdpl.ast.ASTQuery;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.ASTSelectQuery;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventType;
import com.espertech.esper.example.transaction.TransactionSamplePlugin;

import eu.play_project.platformservices.bdpl.parser.BDPLSyntaxCheckProcessor;
import eu.play_project.platformservices.querydispatcher.query.translate.EPLTranslationProcessor;


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
					
						try {
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
									
									query = EPLTranslationProcessor.process(qc, prolog);
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
						}
						
						
						synchronized(lock){
							if(epService != null){
								
								EPStatement testStmt = epService.getEPAdministrator().createEPL(query);
								testStmt.addListener(new TestStmtListener());
								        
								stmts.put(++statementCounter, new EPStatementEntry(query, testStmt));
								System.out.println("\n[Statement "+statementCounter+":\n"+query+"\nis started]");
							}
							else{
								System.out.println("\n[Engine is not started]");
							}
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
					}
					else{
						System.out.println("[:D]");
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
