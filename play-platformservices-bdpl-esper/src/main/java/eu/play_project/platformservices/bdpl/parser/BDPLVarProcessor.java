/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTArrayElement;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVariable;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTBindingsClause;
import org.openrdf.query.parser.bdpl.ast.ASTConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTContextClause;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef2;
import org.openrdf.query.parser.bdpl.ast.ASTIRI;
import org.openrdf.query.parser.bdpl.ast.ASTNumericLiteral;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.ASTRDFLiteral;
import org.openrdf.query.parser.bdpl.ast.ASTStaticArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTStaticArrayDef2;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.ArrayDef;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.Token;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.array.BDPLArrayType;
import eu.play_project.platformservices.bdpl.parser.util.ArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.ArrayTableEntry;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;



/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLVarProcessor {
	
	public static ArrayTable process(ASTOperationContainer qc)
			throws MalformedQueryException{
		VarTableCreator vtCreator = new VarTableCreator();
		
		VarTableCreatorData data = new VarTableCreatorData();
		
		try {
			qc.jjtAccept(vtCreator, data);
			return data.getArrayTable();
			
		} catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class VarTableCreatorData{
		
		private ArrayTable arrayTable = new ArrayTable();
		
		/*
		 * temp variable for saving the source text of array variable
		 */
		private StringBuffer sourceText = new StringBuffer();

		public StringBuffer getSourceText(){
			return sourceText;
		}
		
		public ArrayTable getArrayTable(){
			return arrayTable;
		}
		
	} 
	
	private static class VarTableCreator extends ASTVisitorBase {
		
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
		public Object visit(ASTBindingsClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTQName node, Object data)
			throws VisitorException
		{
			throw new VisitorException("QNames must be resolved before creating variable table.");
		}
		
		/*
		 * visited nodes
		 * 
		 */	
		
		@Override
		public Object visit(ASTContextClause node, Object data)
				throws VisitorException
		{
			if(node.getArrayDef()){
				// the 1st child of ASTContext must be ASTVar !!! Pay attention to grammar file
				String arrayName = ((ASTVar)node.jjtGetChild(0)).getName();
				BDPLArray array = new BDPLArray(null);
				
				// the 2nd child of ASTContextClause must be a ArrayDef. !!! Pay attention to grammar file
				// a ArrayDef has a 'source' property
				Node arrayDefNode = node.jjtGetChild(1);
				BDPLArrayType arrayType = (BDPLArrayType)arrayDefNode.jjtAccept(this, data);
				
				// create new array table entry
				ArrayTableEntry entry = new ArrayTableEntry();
				entry.setArray(array);
				entry.setType(arrayType);
				entry.setSource(((ArrayDef)arrayDefNode).getSource());
				
				try{
					
					ArrayTable table = ((VarTableCreatorData)data).getArrayTable();
					table.add(arrayName, entry);
					
					// here must return data, see SimpleNode.childrenAccept()
					return data;
				}
				catch(BDPLArrayException ae){
					throw new VisitorException(ae.getMessage());
				}
			}
			else{
				return data;
			}
		}
		
		@Override
		public Object visit(ASTArrayDecl node, Object data)
				throws VisitorException
		{
			// ASTArrayDecl must have one ASTArrayVariable node. !!! Pay attention to grammar file
			ASTArrayVariable arrayValNode = node.jjtGetChild(ASTArrayVariable.class);
			BDPLArray array = (BDPLArray)arrayValNode.jjtAccept(this, data);
			
			if(arrayValNode.getSize() == null){
				throw new VisitorException("A size must be declared for a dynamic array.");
			}
			
			// the 1st child of ASTArrayDecl must be a ArrayDef. !!! Pay attention to grammar file
			// a ArrayDef has a 'source' property
			Node arrayDefNode = node.jjtGetChild(0);
			BDPLArrayType arrayType = (BDPLArrayType)arrayDefNode.jjtAccept(this, data);
		
			
			// create new array table entry
			ArrayTableEntry entry = new ArrayTableEntry();
			entry.setArray(array);
			entry.setType(arrayType);
			entry.setSource(((ArrayDef)arrayDefNode).getSource());
			
			try{
				
				ArrayTable table = ((VarTableCreatorData)data).getArrayTable();
				table.add(arrayValNode.getName(), entry);
				
				// here must return data, see SimpleNode.childrenAccept()
				return data;
			}
			catch(BDPLArrayException ae){
				throw new VisitorException(ae.getMessage());
			}
			
		}
		
		@Override
		public Object visit(ASTArrayVariable node, Object data)
				throws VisitorException
		{
			
			BDPLArray ret;
			// the 1st child of ASTArrayVariable must be ASTVar !!! Pay attention to grammar file
			node.setName(((ASTVar)node.jjtGetChild(0)).getName());
			
			// create array object here
			String size = node.getSize();
			if(size == null){
				ret = new BDPLArray(null);
			}
			else{
				try{
					ret = new BDPLArray(Integer.valueOf(size), null);
				}
				catch(NumberFormatException nfe){
					throw new VisitorException("Size of dynamic array could not be parsed as an integer.");
				}
			}
			
			return ret;
		}
		
		@Override
		public Object visit(ASTDynamicArrayDef1 node, Object data)
				throws VisitorException
		{
			/*
			 *	(checked) ?x AS ?x(size) 
			 */
			
			// the 1st child of ASTDynamicArayDef1 must be ASTVar !!! Pay attention to grammar file
			node.setSource(((ASTVar)node.jjtGetChild(0)).getName());
			return BDPLArrayType.DYNAMIC_VAR;
		}
		
		@Override
		public Object visit(ASTDynamicArrayDef2 node, Object data)
				throws VisitorException
		{
			/*
			 * 	{ SUB CONSTRUCT QUERY } AS ?x(size)
			 */
			
			StringBuffer sourceText = ((VarTableCreatorData)data).getSourceText();
			Token token = node.jjtGetFirstToken();
			
			boolean nullToken = false; 
			for(; token != node.jjtGetLastToken(); ){
				if(token != null){
					sourceText.append(token.image+" ");
					token = token.next;
				}
				else{
					nullToken = true;
					break;
				}
			}
			if(!nullToken && token != null){
				sourceText.append(token.image+" ");
			}
			
			node.setSource(sourceText.toString());
			sourceText.delete(0, sourceText.length());
			
			return BDPLArrayType.DYNAMIC_QUERY;
		}
		
		@Override
		public Object visit(ASTStaticArrayDef1 node, Object data)
				throws VisitorException
		{
			/*
			 * 	(1 2 3) AS ?x()
			 */
			
			StringBuffer sourceText = ((VarTableCreatorData)data).getSourceText();
			
			// between every child node ASTArrayElement should be a ";". Pay attention to grammar file.
			for(Node child : node.jjtGetChildren()){
				child.jjtAccept(this, data);
				sourceText.append(";");
			} 
			
			node.setSource(sourceText.toString().trim());
			sourceText.delete(0, sourceText.length());
			
			return BDPLArrayType.STATIC_EXPLICITE;
		}
		
		@Override
		public Object visit(ASTArrayElement node, Object data)
				throws VisitorException
		{	
			StringBuffer sourceText = ((VarTableCreatorData)data).getSourceText();
			
			for(Node child : node.jjtGetChildren()){
				if(child instanceof ASTRDFLiteral){
					ASTRDFLiteral rdfLiteral = (ASTRDFLiteral)child;
					sourceText.append(rdfLiteral.getLabel().getValue());
					if(rdfLiteral.getLang() != null){
						sourceText.append(rdfLiteral.getLang()+" ");
					}
					else if(rdfLiteral.getDatatype() != null){
						sourceText.append("^^"+rdfLiteral.getDatatype().getValue()+" ");
					}
					else{
						sourceText.append(" ");
					}
				}
				else if(child instanceof ASTNumericLiteral){
					ASTNumericLiteral numLiteral = (ASTNumericLiteral)child;
					sourceText.append(numLiteral.getValue());
					if(numLiteral.getDatatype() != null){
						sourceText.append("^^"+numLiteral.getDatatype()+" ");
					}
					else{
						sourceText.append(" ");
					}
				}
				else if(child instanceof ASTIRI){
					sourceText.append(((ASTIRI)child).getValue()+" ");
				}
				else{
					throw new VisitorException("Invalid data type for static arrays.");
				}
			} 
			
			return data;
		}
		
		
		@Override
		public Object visit(ASTStaticArrayDef2 node, Object data)
				throws VisitorException
		{
			/*
			 * 	{ SELECT QUERY } AS ?x()
			 */
			
			StringBuffer sourceText = ((VarTableCreatorData)data).getSourceText();
			Token token = node.jjtGetFirstToken();
			
			boolean nullToken = false; 
			for(; token != node.jjtGetLastToken(); ){
				if(token != null){
					sourceText.append(token.image+" ");
					token = token.next;
				}
				else{
					nullToken = true;
					break;
				}
			}
			if(!nullToken && token != null){
				sourceText.append(token.image+" ");
			}
			
			node.setSource(sourceText.toString());
			sourceText.delete(0, sourceText.length());
			
			return BDPLArrayType.STATIC_QUERY;
		}
	}
}
