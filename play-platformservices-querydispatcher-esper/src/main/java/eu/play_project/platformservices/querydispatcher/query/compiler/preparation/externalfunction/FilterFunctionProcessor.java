/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTAFA;
import org.openrdf.query.parser.bdpl.ast.ASTAFB;
import org.openrdf.query.parser.bdpl.ast.ASTAFC;
import org.openrdf.query.parser.bdpl.ast.ASTArrayFilter;
import org.openrdf.query.parser.bdpl.ast.ASTArrayFilterExpression;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVar;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTContextClause;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTExternalFunctionDecl;
import org.openrdf.query.parser.bdpl.ast.ASTExternalFunctionParameterDecl;
import org.openrdf.query.parser.bdpl.ast.ASTIRI;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTPrimitiveValue;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.ASTRDFLiteral;
import org.openrdf.query.parser.bdpl.ast.ASTString;
import org.openrdf.query.parser.bdpl.ast.ASTSubBDPLQuery;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.VisitorException;







import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.FunctionCompoundExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.FunctionFunctionExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.FunctionSimpleExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.FunctionVarExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.VariableBinder;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;


/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class FilterFunctionProcessor {
	
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
		
		private VariableBinder varBinder;
		
		private boolean hasVariable = false;
		
		private ExternalFunctionProcessorData(BDPLArrayTable arrayTable){
			if(arrayTable == null){
				throw new IllegalArgumentException();
			}
			
			varBinder = new VariableBinder(arrayTable);
		}
		
		private VariableBinder getVarBinder() {
			return this.varBinder;
		}
	}
	
	private static class EFProcessor extends ASTVisitorBase {
		
		private String PARA_TYPE_INT = "int", PARA_TYPE_DECIMAL = "decimal", PARA_TYPE_BOOLEAN = "boolean", 
				PARA_TYPE_LIT = "literal", PARA_TYPE_VAR = "var", PARA_TYPE_ARRAY = "array";
		
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
		public Object visit(ASTBDPLConstruct node, Object data)
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
				BDPLArrayFilter arrayFilter = new BDPLArrayFilter(((ExternalFunctionProcessorData)data).getVarBinder(), (IFunctionExpression<VariableBinder>)expNode.jjtAccept(this, data));
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
				FunctionCompoundExpression expNode = new FunctionCompoundExpression(operator);
				
				Node op1 = node.jjtGetChild(0);
				expNode.addOperand((IFunctionExpression<VariableBinder>)op1.jjtAccept(this, data));
				Node op2 = node.jjtGetChild(1);
				expNode.addOperand((IFunctionExpression<VariableBinder>)op2.jjtAccept(this, data));
				
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
				FunctionCompoundExpression expNode = new FunctionCompoundExpression(operator);
				
				Node op1 = node.jjtGetChild(0);
				expNode.addOperand((IFunctionExpression<VariableBinder>)op1.jjtAccept(this, data));
				Node op2 = node.jjtGetChild(1);
				expNode.addOperand((IFunctionExpression<VariableBinder>)op2.jjtAccept(this, data));
				
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
				FunctionCompoundExpression expNode = new FunctionCompoundExpression(operator);
				
				Node op1 = node.jjtGetChild(0);
				expNode.addOperand((IFunctionExpression<VariableBinder>)op1.jjtAccept(this, data));
				
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
					String t = ((ASTPrimitiveValue) op1).getType();
					
					if(t.equalsIgnoreCase(PARA_TYPE_INT)){
						return new FunctionSimpleExpression(FunctionFunctionExpression.PARA_TYPE_INT, ((ASTPrimitiveValue) op1).getValue());
					}
					else if(t.equalsIgnoreCase(PARA_TYPE_DECIMAL)){
						return new FunctionSimpleExpression(FunctionFunctionExpression.PARA_TYPE_DECIMAL, ((ASTPrimitiveValue) op1).getValue());
					}
					else if(t.equalsIgnoreCase(PARA_TYPE_BOOLEAN)){
						return new FunctionSimpleExpression(FunctionFunctionExpression.PARA_TYPE_BOOLEAN, ((ASTPrimitiveValue) op1).getValue());
					}
					else if(t.equalsIgnoreCase(PARA_TYPE_LIT)){
						ASTRDFLiteral ln = ((ASTPrimitiveValue) op1).jjtGetChild(ASTRDFLiteral.class);
						ASTString sn = ln.getLabel();
						ASTIRI in = ln.getDatatype();
						
						if(in == null){
							return new FunctionSimpleExpression(FunctionFunctionExpression.PARA_TYPE_STR, sn.getValue());
						}
						else{
							String dataType = in.getValue();
							
							if(dataType.equals(XMLSchema.INTEGER)){
								return new FunctionSimpleExpression(FunctionFunctionExpression.PARA_TYPE_INT, sn.getValue());
							}
							else if(dataType.equals(XMLSchema.DECIMAL)){
								return new FunctionSimpleExpression(FunctionFunctionExpression.PARA_TYPE_DECIMAL, sn.getValue());
							}
							else{
								return new FunctionSimpleExpression(FunctionFunctionExpression.PARA_TYPE_STR, sn.getValue());
							}
						}
						
					}
					else{
						throw new VisitorException("ASTAFC has not supported primitive value "+t);
					}
					
				} catch (BDPLFilterException e) {
					throw new VisitorException(e.getMessage());
				}
			}
			else if(op1 instanceof ASTVar){
				((ExternalFunctionProcessorData)data).hasVariable = true;
				return new FunctionVarExpression(((ASTVar) op1).getName());
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
				FunctionFunctionExpression exp;
				
				try {
					exp = new FunctionFunctionExpression(fName.getValue());
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
					p[0] = PARA_TYPE_VAR;
					p[1] = ((ASTVar) child).getName();
				}
				else if(child instanceof ASTArrayVar){
					p[0] = PARA_TYPE_ARRAY;
					ASTVar cchild = (ASTVar)child.jjtGetChild(0);			
					p[1] = cchild.getName();
				}
				else if(child instanceof ASTPrimitiveValue){
					String t = ((ASTPrimitiveValue) child).getType();
					if(t.equalsIgnoreCase(PARA_TYPE_INT)){
						p[0] = FunctionFunctionExpression.PARA_TYPE_INT;
						p[1] = ((ASTPrimitiveValue) child).getValue();
							
					}
					else if(t.equalsIgnoreCase(PARA_TYPE_DECIMAL)){
						p[0] = FunctionFunctionExpression.PARA_TYPE_DECIMAL;
						p[1] = ((ASTPrimitiveValue) child).getValue();
							
					}
					else if(t.equalsIgnoreCase(PARA_TYPE_BOOLEAN)){
						p[0] = FunctionFunctionExpression.PARA_TYPE_BOOLEAN;
						p[1] = ((ASTPrimitiveValue) child).getValue();
					}
					else if(t.equalsIgnoreCase(PARA_TYPE_LIT)){
						ASTRDFLiteral ln = ((ASTPrimitiveValue) child).jjtGetChild(ASTRDFLiteral.class);
						ASTString sn = ln.getLabel();
						p[1] = sn.getValue();
						ASTIRI in = ln.getDatatype();
						
						if(in == null){
							p[0] = FunctionFunctionExpression.PARA_TYPE_STR;
						}
						else{
							String dataType = in.getValue();
							
							if(dataType.equals(XMLSchema.INTEGER)){
								p[0] = FunctionFunctionExpression.PARA_TYPE_INT;
							}
							else if(dataType.equals(XMLSchema.DECIMAL)){
								p[0] = FunctionFunctionExpression.PARA_TYPE_DECIMAL;
							}
							else{
								p[0] = FunctionFunctionExpression.PARA_TYPE_STR;
							}
						}
						
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
