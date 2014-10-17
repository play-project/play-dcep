/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct;

import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLWhereClause;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TranslateException;

/**
 * @author ningyuan 
 * 
 * Oct 16, 2014
 *
 */
public class ConstructTranslationProcessor {
	
	public static void process(ASTOperationContainer qc, String prologText, BDPLArrayTable arrayTable)
			throws TranslateException{
		ConstructTranslator translator = new ConstructTranslator();
		
		ConstructTranslatorData data = new ConstructTranslatorData(prologText, arrayTable);
		
		try {
			qc.jjtAccept(translator, data);
			
			
		} catch (VisitorException e) {
			throw new TranslateException(e.getMessage());
		}
	}
	
	private static class ConstructTranslatorData{
		private String prologText;
		
		private BDPLArrayTable arrayTable;
		
		private ConstructTranslatorData(String p, BDPLArrayTable at){
			prologText = p;
			arrayTable = at;
		}
	}
	
	private static class ConstructTranslator extends ASTVisitorBase {
		
		/*
		 * skipped nodes
		 */
		@Override
		public Object visit(ASTBaseDecl node, Object data)
				throws VisitorException
		{
			
			return data;
		}
		
		@Override
		public Object visit(ASTPrefixDecl node, Object data)
				throws VisitorException
		{
			
			return data;
		}
		
		@Override
		public Object visit(ASTDatasetClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTDynamicArrayDecl node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTBDPLWhereClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTQName node, Object data)
			throws VisitorException
		{
			throw new VisitorException("QNames must be resolved before EPL translation.");
		}
		
		
		
		
		/*
		 * visited nodes
		 */
		
		@Override
		public Object visit(ASTBDPLConstruct node, Object data)
				throws VisitorException
		{
			//TODO 1. check array exists in array table
			return data;
		}
		
	}
}
