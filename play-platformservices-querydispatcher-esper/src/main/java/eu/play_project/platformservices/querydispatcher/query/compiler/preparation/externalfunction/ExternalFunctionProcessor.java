/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction;

import java.util.ArrayList;
import java.util.List;


import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTAFA;
import org.openrdf.query.parser.bdpl.ast.ASTAFB;
import org.openrdf.query.parser.bdpl.ast.ASTAFC;
import org.openrdf.query.parser.bdpl.ast.ASTArrayFilter;
import org.openrdf.query.parser.bdpl.ast.ASTArrayFilterExpression;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVar;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTContextClause;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTExternalFunctionDecl;
import org.openrdf.query.parser.bdpl.ast.ASTExternalFunctionParameterDecl;
import org.openrdf.query.parser.bdpl.ast.ASTIRI;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTPrimitiveValue;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.ASTString;
import org.openrdf.query.parser.bdpl.ast.ASTSubBDPLQuery;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.VisitorException;


import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.ExternalFunctionCompoundExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.ExternalFunctionFunctionExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.ExternalFunctionSimpleExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.ExternalFunctionVarExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.VariableBinder;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;


/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class ExternalFunctionProcessor {
	
	public static void process(ASTOperationContainer qc, BDPLArrayTable arrayTable)
			throws MalformedQueryException{
		EFProcessor precessor = new EFProcessor();
		
		ExternalFunctionProcessorData data = new ExternalFunctionProcessorData(arrayTable);
		
		try {
			qc.jjtAccept(precessor, data);
			
		} catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class ExternalFunctionProcessorData{
		
		private final BDPLArrayTable arrayTable;
		
		private VariableBinder varBinder;
		
		boolean hasVariable = false;
		
		ExternalFunctionProcessorData(BDPLArrayTable arrayTable){
			if(arrayTable == null){
				throw new IllegalArgumentException();
			}
			
			this.arrayTable = arrayTable;
			varBinder = new VariableBinder(arrayTable);
		}
		
		VariableBinder getVarBinder() {
			return this.varBinder;
		}
	}
	
	private static class EFProcessor extends ASTVisitorBase {
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
		public Object visit(ASTContextClause node, Object data)
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
		
		@Override
		public Object visit(ASTSubBDPLQuery node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		
		/*
		 * visit nodes
		 */
		@Override
		public Object visit(ASTArrayFilter node, Object data)
				throws VisitorException
		{	
			
			// pay attension to grammar file
			ASTArrayFilterExpression expNode = node.jjtGetChild(ASTArrayFilterExpression.class);
			try {
				BDPLArrayFilter arrayFilter = new BDPLArrayFilter(((ExternalFunctionProcessorData)data).getVarBinder(), (IExternalFunctionExpression<VariableBinder>)expNode.jjtAccept(this, data));
				arrayFilter.setHasVariable(((ExternalFunctionProcessorData)data).hasVariable);
				((ExternalFunctionProcessorData)data).hasVariable = false;
				
				// ASTArrayFilter.setFilterObject() must be called
				node.setFilterOjbect(arrayFilter);
			} catch (BDPLFilterException e) {
				throw new VisitorException(e.getMessage());
			}
			
			return data;
		}
		
		
		@Override
		public Object visit(ASTArrayFilterExpression node, Object data)
				throws VisitorException
		{
			// pay attension to grammar file
			String operator = node.getOperator();
			if(operator != null){
				ExternalFunctionCompoundExpression expNode = new ExternalFunctionCompoundExpression(operator);
				
				Node op1 = node.jjtGetChild(0);
				expNode.addOperand((IExternalFunctionExpression<VariableBinder>)op1.jjtAccept(this, data));
				Node op2 = node.jjtGetChild(1);
				expNode.addOperand((IExternalFunctionExpression<VariableBinder>)op2.jjtAccept(this, data));
				
				return expNode;
			}
			else{
				ASTAFA expNode = node.jjtGetChild(ASTAFA.class);
				return expNode.jjtAccept(this, data);
			}
		}
		
		@Override
		public Object visit(ASTAFA node, Object data)
				throws VisitorException
		{
			// pay attension to grammar file
			String operator = node.getOperator();
			if(operator != null){
				ExternalFunctionCompoundExpression expNode = new ExternalFunctionCompoundExpression(operator);
				
				Node op1 = node.jjtGetChild(0);
				expNode.addOperand((IExternalFunctionExpression<VariableBinder>)op1.jjtAccept(this, data));
				Node op2 = node.jjtGetChild(1);
				expNode.addOperand((IExternalFunctionExpression<VariableBinder>)op2.jjtAccept(this, data));
				
				return expNode;
			}
			else{
				ASTAFB expNode = node.jjtGetChild(ASTAFB.class);
				return expNode.jjtAccept(this, data);
			}
		}
		
		@Override
		public Object visit(ASTAFB node, Object data)
				throws VisitorException
		{
			// pay attension to grammar file
			String operator = node.getOperator();
			if(operator != null){
				ExternalFunctionCompoundExpression expNode = new ExternalFunctionCompoundExpression(operator);
				
				Node op1 = node.jjtGetChild(0);
				expNode.addOperand((IExternalFunctionExpression<VariableBinder>)op1.jjtAccept(this, data));
				
				return expNode;
			}
			else{
				ASTAFC expNode = node.jjtGetChild(ASTAFC.class);
				return expNode.jjtAccept(this, data);
			}
		}
		
		@Override
		public Object visit(ASTAFC node, Object data)
				throws VisitorException
		{	
			// pay attension to grammar file
			Node op1 = node.jjtGetChild(0);
			
			if(op1 instanceof ASTPrimitiveValue){
				try {
					return new ExternalFunctionSimpleExpression(((ASTPrimitiveValue) op1).getType(), ((ASTPrimitiveValue) op1).getValue());
				} catch (BDPLFilterException e) {
					throw new VisitorException(e.getMessage());
				}
			}
			else if(op1 instanceof ASTVar){
				((ExternalFunctionProcessorData)data).hasVariable = true;
				return new ExternalFunctionVarExpression(((ASTVar) op1).getName());
			}
			else if(op1 instanceof ASTString){
				try {
					return new ExternalFunctionSimpleExpression(ExternalFunctionSimpleExpression.VALUE_TYPE_STR, ((ASTString) op1).getValue());
				} catch (BDPLFilterException e) {
					throw new VisitorException(e.getMessage());
				}
			}
			else{
				return op1.jjtAccept(this, data);
			}
		}
		
		@Override
		public Object visit(ASTExternalFunctionDecl node, Object data)
				throws VisitorException
		{	
			// pay attension to grammar file
			ASTIRI fName = node.jjtGetChild(ASTIRI.class);
			if(fName != null){
				ExternalFunctionFunctionExpression exp;
				
				try {
					exp = new ExternalFunctionFunctionExpression(fName.getValue());
				} catch (BDPLFilterException e) {
					throw new VisitorException(e.getMessage());
				}
				
				ASTExternalFunctionParameterDecl paraDecl = node.jjtGetChild(ASTExternalFunctionParameterDecl.class);
				
				try {
					exp.setParameters((List<String[]>)paraDecl.jjtAccept(this, data));
				} catch (BDPLFilterException e) {
					throw new VisitorException(e.getMessage());
				}
				
				return exp;
			}
			else{
				throw new VisitorException("External function dose not have a valid name");
			}
		}
		
		@Override
		public Object visit(ASTExternalFunctionParameterDecl node, Object data)
				throws VisitorException
		{	
			List<String[]> ret = new ArrayList<String[]>();
			
			//pay attension to grammar file
			List<Node> children = node.jjtGetChildren();
			
			for(int i = 0; i < children.size(); i++){
				Node child = children.get(i);
				String [] p = new String[2];
				ret.add(p);
				
				if(child instanceof ASTVar){
					p[0] = ExternalFunctionFunctionExpression.PARA_TYPE_VAR;
					p[1] = ((ASTVar) child).getName();
				}
				else if(child instanceof ASTArrayVar){
					p[0] = ExternalFunctionFunctionExpression.PARA_TYPE_ARRAY;
					ASTVar cchild = (ASTVar)child.jjtGetChild(0);			
					p[1] = cchild.getName();
				}
				else if(child instanceof ASTString){
					p[0] = ExternalFunctionFunctionExpression.PARA_TYPE_STR;
					//TODO check
					p[1] = ((ASTString) child).getValue();
				}
				else if(child instanceof ASTPrimitiveValue){
					String t = ((ASTPrimitiveValue) child).getType();
					if(t.equalsIgnoreCase(ExternalFunctionFunctionExpression.PARA_TYPE_INT)){
						p[0] = ExternalFunctionFunctionExpression.PARA_TYPE_INT;
						p[1] = ((ASTPrimitiveValue) child).getValue();
							System.out.println(p[1]);
					}
					else if(t.equalsIgnoreCase(ExternalFunctionFunctionExpression.PARA_TYPE_DECIMAL)){
						p[0] = ExternalFunctionFunctionExpression.PARA_TYPE_DECIMAL;
						p[1] = ((ASTPrimitiveValue) child).getValue();
							System.out.println(p[1]);
					}
					else if(t.equalsIgnoreCase(ExternalFunctionFunctionExpression.PARA_TYPE_BOOLEAN)){
						p[0] = ExternalFunctionFunctionExpression.PARA_TYPE_BOOLEAN;
						p[1] = ((ASTPrimitiveValue) child).getValue();
					}
					else{
						throw new VisitorException("ASTExternalFunctionParameterDecl has not supported primitive value "+t);
					}
				}
				else{
					throw new VisitorException("ASTExternalFunctionParameterDecl has unknown child node");
				}
			}
			
			return ret;
		}
	}
}
