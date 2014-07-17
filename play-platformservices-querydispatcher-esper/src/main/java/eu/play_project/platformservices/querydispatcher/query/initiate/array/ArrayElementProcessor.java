/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.initiate.array;


import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTBindingsClause;
import org.openrdf.query.parser.bdpl.ast.ASTConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTProjectionElem;
import org.openrdf.query.parser.bdpl.ast.ASTSelect;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.ASTWhereClause;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.querydispatcher.query.initiate.util.InitiateException;


/**
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public class ArrayElementProcessor {
	
	public static List<String> process(ASTOperationContainer qc) throws InitiateException{
		Processor processor = new Processor();
		ArrayElementData data = new ArrayElementData();
		
		try{
			qc.jjtAccept(processor, data);
			return data.getVarNames();
		} 
		catch (VisitorException e) {
			throw new InitiateException("");
		}
	}
	
	private static class ArrayElementData{
		
		private List<String> varNames = new ArrayList<String>();
		
		public List<String> getVarNames(){
			return varNames;
		}
	}
	
	private static class Processor extends ASTVisitorBase {
		/*
		 * skipped nodes
		 *  
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
		public Object visit(ASTConstruct node, Object data)
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
		public Object visit(ASTWhereClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTBindingsClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		
		
		/*
		 * visited nodes
		 * 
		 */	
		@Override
		public Object visit(ASTSelect node, Object data)
				throws VisitorException
		{	
			//TODO reduced distinct; select * ; dynamic
			
			for(Node child : node.jjtGetChildren()){
				child.jjtAccept(this, data);
			}
			
			return data;
		}
		
		@Override
		public Object visit(ASTProjectionElem node, Object data)
				throws VisitorException
		{
			List<String> varNames = ((ArrayElementData)data).getVarNames();
			
			varNames.add(((ASTVar)node.jjtGetChild(ASTVar.class)).getName());
			
			return data;
		}
	}
}
