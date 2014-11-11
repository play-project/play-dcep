/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import eu.play_project.platformservices.querydispatcher.query.compiler.generation.GenerationPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.InitiationPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.PreparationPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.TranslationPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompilerData;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLQuery;
import eu.play_project.platformservices.querydispatcher.query.extension.function.FunctionManager;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionParameterTypeException;

/**
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class BDPLCompiler {
	
	private BDPLCompilerPhase<BDPLCompilerData> phase;
	
	public BDPLCompiler(){
		BDPLCompilerPhase<BDPLCompilerData> next, temp;
		phase = new PreparationPhase();
		next = new TranslationPhase();
		phase.setNextPhase(next);
		temp = next;
		next = new InitiationPhase();
		temp.setNextPhase(next);
		temp = next;
		next = new GenerationPhase();
		temp.setNextPhase(next);
		
	}
	
	/**
	 * thread safe compile bdpl query
	 * 
	 * @param queryStr
	 * @param baseURI
	 * @return
	 * @throws BDPLCompileException
	 */
	public IBDPLQuery compile(String queryStr, String baseURI) throws BDPLCompileException {
		
		BDPLCompilerData data = new BDPLCompilerData(baseURI, queryStr);
		phase.handle(data);
		
		return data.getCompiledQuery();
	}
	
	/*public static IBDPLQuery compile(String queryStr, String baseURI) throws BDPLCompileException {
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
				
				
			BDPLArrayTable arrayTable = BDPLArrayVarProcessor.process(qc, varTable, prologText);
				System.out.println("\nArrayTable: ");
				for(String key : arrayTable.keySet()){
					BDPLArrayTableEntry arrayEntry = arrayTable.get(key);
					System.out.println(key+"   "+arrayEntry.getSource());
				}
			
			ExternalFunctionProcessor.process(qc, arrayTable);
				
			EPLTranslationData tData = EPLTranslationProcessor.process(qc, prologText);
				System.out.println("\nepl:\n"+tData.getEpl());
			
			//String listenerQuery = EPLListenerProcessor.process(qc, prologText);
				//System.out.println("\nListener query:\n"+listenerQuery);
				
				
			ArrayInitiator arrayInitiator = new ArrayInitiator(new DefaultArrayMaker());
			
			SubQueryTable subQueryTable = arrayInitiator.initiate(arrayTable);
			
			
			
			RealTimeResults realTimeResults = new RealTimeResults();
			
			RealTimeResultBindingData rtbData = new RealTimeResultBindingData(varTable.getRealTimeCommonVars(), realTimeResults, subQueryTable.getEntryToSelf());
			
			tData.getInjectParameterMapping().put(EPLTranslationData.INJECT_PARA_REALTIMERESULT_BINDING_DATA, rtbData);
			
			//UpdateListener listener = new RealTimeResultBindingListener2(varTable.getRealTimeCommonVars(), listenerQuery, subQueryTable.getEntryToSelf());
			UpdateListener listener = new RealTimeResultListener(realTimeResults, tData.getEventPatternFilters());
			
			ret = new DefaultBDPLPreparedQuery(tData.getEpl(), tData.getInjectParameterMapping(), tData.getInjectParams(), listener, subQueryTable);
			
		}
		catch (ParseException e) {
			throw new BDPLCompileException(e.getMessage());
		}
		catch (TokenMgrError e) {
			throw new BDPLCompileException(e.getMessage());
		}
		catch(MalformedQueryException e){
			e.printStackTrace();
			throw new BDPLCompileException(e.getMessage());
		}
		catch(InitiateException e){
			throw new BDPLCompileException(e.getMessage());
		}
		
		return ret; 
	}*/
	
	
	
	public static void main(String[] args) throws IOException{
		FunctionManager fManager = FunctionManager.getInstance();
		
		try {
			fManager.initiateTable();
		} catch (FunctionParameterTypeException e) {
			System.out.println(e.getMessage());
		}
		
		BDPLCompiler compiler = new BDPLCompiler();
		
		System.out.println("Your BDPL query:");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		StringBuilder buf = new StringBuilder();
		String line = null;
		
		int emptyLineCount = 0;
		while ((line = in.readLine()) != null) {
			if (line.length() > 0) {
				emptyLineCount = 0;
				buf.append(' ').append(line).append('\n');
			}
			else {
				emptyLineCount++;
			}

			if (emptyLineCount == 2) {
				emptyLineCount = 0;
				String queryStr = buf.toString().trim();
				if (queryStr.length() > 0) {
					try {
						
						compiler.compile(queryStr, null);
						
						System.out.println();

					}
					catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
				buf.setLength(0);
			}
		}
	}
}
