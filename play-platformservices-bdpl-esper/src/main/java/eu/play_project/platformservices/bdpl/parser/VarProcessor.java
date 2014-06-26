/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVariable;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.util.VarTable;



/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class VarProcessor {
	
	public static VarTable process(ASTOperationContainer qc)
			throws MalformedQueryException{
		VarTableCreator vtCreator = new VarTableCreator();
		
		VarTable varTable = new VarTable();
		try {
			qc.jjtAccept(vtCreator, varTable);
			return varTable;
			
		} catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class VarTableCreator extends ASTVisitorBase {
		
		@Override
		public Object visit(ASTArrayDecl node, Object data)
				throws VisitorException
		{
			// ASTArrayDecl must have one ASTArrayVariable node. !!! Pay attention to grammar file
			ASTArrayVariable arrayValNode = node.jjtGetChild(ASTArrayVariable.class);
			arrayValNode.jjtAccept(this, data);
			
			// the 1st child of ASTArrayDecl must be a definition. !!! Pay attention to grammar file
			node.jjtGetChild(0).jjtAccept(this, data);
			
			return null;
		}
		
		@Override
		public Object visit(ASTArrayVariable node, Object data)
				throws VisitorException
		{
			super.visit(node, data);
			
			// the 1st child of ASTArrayVariable must be ASTVar !!! Pay attention to grammar file
			String arrayVarName = ((ASTVar)node.jjtGetChild(0)).getName();
			
			return null;
		}
	}
}
