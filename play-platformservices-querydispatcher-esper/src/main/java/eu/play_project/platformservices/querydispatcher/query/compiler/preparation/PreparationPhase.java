/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation;

import java.util.Set;

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
import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerException;
import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.FilterFunctionProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLCompilerData;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class PreparationPhase extends BDPLCompilerPhase<DefaultBDPLCompilerData> {
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase#process()
	 */
	@Override
	protected void process(DefaultBDPLCompilerData data) throws BDPLCompilerException {
		
		try {
			data.setQueryContainer(SyntaxTreeBuilder.parseQuery(data.getBDPLQuery()));
			StringEscapesProcessor.process(data.getQueryContainer());
			BaseDeclProcessor.process(data.getQueryContainer(), data.getBaseURI());
			PrefixDeclProcessor.process(data.getQueryContainer());
			WildcardProjectionProcessor.process(data.getQueryContainer());
			// XXX not useful in bdpl, because not use basic graph pattern
			Set<String> bn = BlankNodeVarProcessor.process(data.getQueryContainer());
				
			DatasetDeclProcessor.process(data.getQueryContainer());
			
			data.setPrologText(BDPLSyntaxCheckProcessor.process(data.getQueryContainer()));
			
			data.setVarTable(BDPLVarProcessor.process(data.getQueryContainer()));
				
			data.setArrayTable(BDPLArrayVarProcessor.process(data.getQueryContainer(), data.getVarTable(), data.getPrologText()));
		
			FilterFunctionProcessor.process(data.getQueryContainer(), data.getArrayTable());
			
		}
		catch (MalformedQueryException | TokenMgrError | ParseException e) {
			throw new BDPLCompilerException(e.getMessage());
		}
	}

}
