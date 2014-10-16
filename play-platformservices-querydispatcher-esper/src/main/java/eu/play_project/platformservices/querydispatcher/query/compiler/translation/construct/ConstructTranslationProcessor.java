/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct;

import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;

import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TranslateException;

/**
 * @author ningyuan 
 * 
 * Oct 16, 2014
 *
 */
public class ConstructTranslationProcessor {
	
	public static void process(ASTOperationContainer qc, String prologText)
			throws TranslateException{
		
	}
	
	private static class ConstructTranslator extends ASTVisitorBase {
		
	}
}
