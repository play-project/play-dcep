/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

import java.util.Map;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.DatasetDeclProcessor;
import org.openrdf.query.parser.bdpl.PrefixDeclProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.TupleExprBuilder;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.bdpl.parser.BDPLArrayVarProcessor;
import eu.play_project.platformservices.bdpl.parser.BDPLSyntaxCheckProcessor;
import eu.play_project.platformservices.bdpl.parser.BDPLVarProcessor;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.bdpl.parser.util.BDPLVarTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.RealTimeResultBindingListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.ArrayInitiator;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.DefaultArrayMaker;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.EPLTranslationProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.TempReturn;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLQuery;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLQuery;

/**
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class BDPLCompiler {
	
	public static IBDPLQuery compile(String queryStr, String baseURI) {
		IBDPLQuery ret = null;
		
		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
			StringEscapesProcessor.process(qc);
			BaseDeclProcessor.process(qc, baseURI);
			Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
			WildcardProjectionProcessor.process(qc);
			BlankNodeVarProcessor.process(qc);
			Dataset dataset = DatasetDeclProcessor.process(qc);
			
			
			BDPLSyntaxCheckProcessor.process(qc);
			
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
					System.out.println(key+" "+arrayEntry.getSource());
				}
			
			String prologText = BDPLSyntaxCheckProcessor.process(qc);
			
			TempReturn tempReturn = EPLTranslationProcessor.process(qc, prologText);
			
			ArrayInitiator arrayInitiator = new ArrayInitiator(new DefaultArrayMaker());
			
			SubQueryTable subQueryTable = arrayInitiator.initiate(arrayTable);
			
			UpdateListener listener = new RealTimeResultBindingListener(varTable.getRealTimeCommonVars(), tempReturn.getMatchedPatternSparql(), subQueryTable.getEntryToSelf());
			
			ret = new DefaultBDPLQuery(tempReturn.getEpl(), listener, subQueryTable);
			
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		catch (TokenMgrError e) {
			e.printStackTrace();
		}
		catch(MalformedQueryException e){
			e.printStackTrace();
		}
		catch(InitiateException e){
			e.printStackTrace();
		}
		
		return ret; 
	}
		
	
}
