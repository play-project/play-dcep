/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.DatasetDeclProcessor;
import org.openrdf.query.parser.bdpl.PrefixDeclProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;


import com.espertech.esper.client.UpdateListener;


import eu.play_project.platformservices.bdpl.parser.BDPLArrayVarProcessor;
import eu.play_project.platformservices.bdpl.parser.BDPLSyntaxCheckProcessor;
import eu.play_project.platformservices.bdpl.parser.BDPLVarProcessor;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.bdpl.parser.util.BDPLVarTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.EPLListenerProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.RealTimeResultBindingListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.RealTimeResultBindingListener2;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.RealTimeResultListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResultBindingData;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResults;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.ArrayInitiator;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.DefaultArrayMaker;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.EPLTranslationProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.EPLTranslationData;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLPreparedQuery;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLQuery;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLQuery;

/**
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class BDPLCompiler {
	
	public static IBDPLQuery compile(String queryStr, String baseURI) throws BDPLCompileException {
		IBDPLQuery ret = null;
		
		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
			StringEscapesProcessor.process(qc);
			BaseDeclProcessor.process(qc, baseURI);
			Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
			WildcardProjectionProcessor.process(qc);
			BlankNodeVarProcessor.process(qc);
			Dataset dataset = DatasetDeclProcessor.process(qc);
			
			
			String prologText = BDPLSyntaxCheckProcessor.process(qc);
			
			BDPLVarTable varTable = BDPLVarProcessor.process(qc);
				System.out.println("Construct variables: ");
				for(String var : varTable.getConstructVars()){
					System.out.print(var+"   ");
				}
				System.out.println("\nCommon variables: ");
				for(String var : varTable.getRealTimeCommonVars()){
					System.out.print(var+"   ");
				}
				
				
			BDPLArrayTable arrayTable = BDPLArrayVarProcessor.process(qc, varTable);
				System.out.println("\nArrayTable: ");
				for(String key : arrayTable.keySet()){
					BDPLArrayTableEntry arrayEntry = arrayTable.get(key);
					System.out.println(key+"   "+arrayEntry.getSource());
				}
			
			
			EPLTranslationData tData = EPLTranslationProcessor.process(qc, prologText);
				System.out.println("\nepl:\n"+tData.getEpl());
			
			//String listenerQuery = EPLListenerProcessor.process(qc, prologText);
				//System.out.println("\nListener query:\n"+listenerQuery);
			
				
				
			ArrayInitiator arrayInitiator = new ArrayInitiator(new DefaultArrayMaker());
			
			SubQueryTable subQueryTable = arrayInitiator.initiate(arrayTable);
			
			
			
			RealTimeResults realTimeResults = new RealTimeResults();
			RealTimeResultBindingData rtbData = new RealTimeResultBindingData(varTable.getRealTimeCommonVars(), realTimeResults, subQueryTable.getEntryToSelf());
			
			Map<Integer, Object> injectParaMapping = new HashMap<Integer, Object>();
			injectParaMapping.put(EPLTranslationData.INJECT_PARA_REALTIMERESULT_BINDING_DATA, rtbData);
			
			
			//UpdateListener listener = new RealTimeResultBindingListener2(varTable.getRealTimeCommonVars(), listenerQuery, subQueryTable.getEntryToSelf());
			UpdateListener listener = new RealTimeResultListener(realTimeResults, arrayTable);
			
			ret = new DefaultBDPLPreparedQuery(tData.getEpl(), injectParaMapping, tData.getInjectParams(), listener, subQueryTable);
			
		}
		catch (ParseException e) {
			throw new BDPLCompileException(e.getMessage());
		}
		catch (TokenMgrError e) {
			throw new BDPLCompileException(e.getMessage());
		}
		catch(MalformedQueryException e){
			throw new BDPLCompileException(e.getMessage());
		}
		catch(InitiateException e){
			throw new BDPLCompileException(e.getMessage());
		}
		
		return ret; 
	}
		
	
}
