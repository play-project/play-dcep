/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.DatasetDeclProcessor;
import org.openrdf.query.parser.bdpl.PrefixDeclProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;

import eu.play_project.platformservices.bdpl.parser.BDPLArrayVarProcessor;
import eu.play_project.platformservices.bdpl.parser.BDPLSyntaxCheckProcessor;
import eu.play_project.platformservices.bdpl.parser.BDPLVarProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.ExternalFunctionProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompilerData;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class PreparationPhase extends BDPLCompilerPhase<BDPLCompilerData> {
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase#process()
	 */
	@Override
	protected void process(BDPLCompilerData data) throws BDPLCompileException {
		
		try {
			data.setQueryContainer(SyntaxTreeBuilder.parseQuery(data.getBdplQuery()));
			StringEscapesProcessor.process(data.getQueryContainer());
			BaseDeclProcessor.process(data.getQueryContainer(), data.getBaseURI());
			PrefixDeclProcessor.process(data.getQueryContainer());
			WildcardProjectionProcessor.process(data.getQueryContainer());
			BlankNodeVarProcessor.process(data.getQueryContainer());
			DatasetDeclProcessor.process(data.getQueryContainer());
			
			data.setPrologText(BDPLSyntaxCheckProcessor.process(data.getQueryContainer()));
			
			data.setVarTable(BDPLVarProcessor.process(data.getQueryContainer()));
				
			data.setArrayTable(BDPLArrayVarProcessor.process(data.getQueryContainer(), data.getVarTable(), data.getPrologText()));
		
			ExternalFunctionProcessor.process(data.getQueryContainer(), data.getArrayTable());
			
		}
		catch (MalformedQueryException | TokenMgrError | ParseException e) {
			throw new BDPLCompileException(e.getMessage());
		}
	}

}
