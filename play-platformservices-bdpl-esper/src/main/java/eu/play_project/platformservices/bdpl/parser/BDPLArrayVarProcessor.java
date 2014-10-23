/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import java.util.List;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVar;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTArrayElement;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTBindingsClause;
import org.openrdf.query.parser.bdpl.ast.ASTContextClause;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef2;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef3;
import org.openrdf.query.parser.bdpl.ast.ASTExternalFunctionParameterDecl;
import org.openrdf.query.parser.bdpl.ast.ASTIRI;
import org.openrdf.query.parser.bdpl.ast.ASTNumericLiteral;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.ASTRDFLiteral;
import org.openrdf.query.parser.bdpl.ast.ASTStaticArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTStaticArrayDef2;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.IArrayDecl;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.bdpl.ast.Token;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayType;
import eu.play_project.platformservices.bdpl.parser.util.BDPLVarTable;



/**
 * A processor of BDPL grammatic tree. It processes all array variables declared in a BDPL query
 * and create BDPLArrayTable.
 * 
 * Before calling this processor, the compiler data structure, BDPLVarTable and text of prolog
 * must be available.
 * 
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLArrayVarProcessor {
	
	/**
	 * 
	 * @param qc
	 * @param varTable
	 * @param prologText
	 * @return
	 * @throws MalformedQueryException
	 */
	public static BDPLArrayTable process(ASTOperationContainer qc, BDPLVarTable varTable, String prologText)
			throws MalformedQueryException{
		ArrayTableCreator atCreator = new ArrayTableCreator(prologText);
		
		ArrayTableCreatorData data = new ArrayTableCreatorData(varTable.getRealTimeCommonVars());
		
		try {
			qc.jjtAccept(atCreator, data);
			BDPLArrayTable arrayTable = data.getArrayTable();
				
				// for test
				System.out.println("\nBDPLArrayTable arrayTable: ");
				for(String key : arrayTable.keySet()){
					BDPLArrayTableEntry arrayEntry = arrayTable.get(key);
					System.out.println(key+"   "+arrayEntry.getSource());
				}

			return arrayTable;
			
		} catch (VisitorException e) {
			
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class ArrayTableCreatorData{
		// index of anonymous BDPL array
		private int anonymousIndex = 0;
		
		private BDPLArrayTable arrayTable = new BDPLArrayTable();
		
		// temp variable for saving the source text of array variable
		private StringBuffer sourceText = new StringBuffer();
		
		// variables in real time event pattern (the content should not be changed)
		private final Set<String> realTimeCommonVars;
		
		private Set<String> getRealTimeCommonVars() {
			return this.realTimeCommonVars;
		}

		private ArrayTableCreatorData(Set<String> realTimeCommonVars){
			this.realTimeCommonVars = realTimeCommonVars;
		}
		
		private StringBuffer getSourceText(){
			return sourceText;
		}
		
		private BDPLArrayTable getArrayTable(){
			return arrayTable;
		}
		
	} 
	
	private static class ArrayTableCreator extends ASTVisitorBase {
		
		private String prologText;
		
		public ArrayTableCreator(String p){
			prologText = p;
		}
		
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
		
		// ignore all array variables in bdpl construct clause
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
		public Object visit(ASTBindingsClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTQName node, Object data)
			throws VisitorException
		{
			throw new VisitorException("QNames must be resolved before creating array table.");
		}
		
		
		
		/*
		 * visited nodes
		 * 
		 */	
		@Override
		public Object visit(ASTContextClause node, Object data)
				throws VisitorException
		{	
			if(node.isStaticArrayDecl()){
				
				
				// the 1st child of ASTContext must be ASTVar !!! Pay attention to grammar file
				String arrayName = ((ASTVar)node.jjtGetChild(0)).getName();
				
				// the 2nd child of ASTContextClause must be an IArrayDecl. !!! Pay attention to grammar file
				// an IArrayDecl has a 'source' property
				Node IarrayDecl = node.jjtGetChild(1);
				BDPLArrayType arrayType = (BDPLArrayType)IarrayDecl.jjtAccept(this, data);
				
				// create new array table entry for static array
				BDPLArrayTableEntry entry = new BDPLArrayTableEntry();
				entry.setName(arrayName);
				entry.setType(arrayType);
				entry.setSource(((IArrayDecl)IarrayDecl).getSource());
				
				try{
					
					BDPLArrayTable table = ((ArrayTableCreatorData)data).getArrayTable();
					table.add(arrayName, entry);
					
					// here must return data, see SimpleNode.childrenAccept()
					return data;
				}
				catch(BDPLArrayException ae){
					throw new VisitorException(ae.getMessage());
				}
			}
			else{
				//TODO other content in context clause
				return data;
			}
		}
		
		@Override
		public Object visit(ASTDynamicArrayDecl node, Object data)
				throws VisitorException
		{
			// ASTDynamicArrayDecl must have one ASTArrayVariable node. !!! Pay attention to grammar file
			ASTArrayVar arrayValNode = node.jjtGetChild(ASTArrayVar.class);
			BDPLArray array = (BDPLArray)arrayValNode.jjtAccept(this, data);
			
			if(arrayValNode.getSize() == null){
				throw new VisitorException("A size must be declared for a dynamic array.");
			}
			
			// the 2nd child of ASTArrayDecl must be an IArrayDecl. !!! Pay attention to grammar file
			// an IArrayDecl has a 'source' property
			Node IarrayDecl = node.jjtGetChild(1);
			BDPLArrayType arrayType = (BDPLArrayType)IarrayDecl.jjtAccept(this, data);
		
			
			// create new array table entry for dynamic array
			BDPLArrayTableEntry entry = new BDPLArrayTableEntry();
			entry.setName(arrayValNode.getName());
			entry.setArray(array);
			entry.setType(arrayType);
			entry.setSource(((IArrayDecl)IarrayDecl).getSource());
			
			try{
				
				BDPLArrayTable table = ((ArrayTableCreatorData)data).getArrayTable();
				table.add(arrayValNode.getName(), entry);
				
				// here must return data, see SimpleNode.childrenAccept()
				return data;
			}
			catch(BDPLArrayException ae){
				throw new VisitorException(ae.getMessage());
			}
			
		}
		
		@Override
		public Object visit(ASTExternalFunctionParameterDecl node, Object data)
				throws VisitorException
		{
			
			BDPLArrayTable table = ((ArrayTableCreatorData)data).getArrayTable();
			
			List<ASTArrayVar> arrayNodes = node.jjtGetChildren(ASTArrayVar.class);
			for(ASTArrayVar arrayNode : arrayNodes){
				ASTVar varNode = arrayNode.jjtGetChild(ASTVar.class);
				if(!table.contain(varNode.getName())){
					throw new VisitorException("External function contains undefined array variable ?"+varNode.getName()+"()");
				}
			}
			
			List<ASTStaticArrayDef1> ayArrayNodes = node.jjtGetChildren(ASTStaticArrayDef1.class);
			
			StringBuffer arrayName = new StringBuffer();
			for(ASTStaticArrayDef1 ayArrayNode : ayArrayNodes){
				
				
				BDPLArrayType arrayType = (BDPLArrayType)ayArrayNode.jjtAccept(this, data);
			
				arrayName.append("_:");
				int index = ((ArrayTableCreatorData)data).anonymousIndex;
				while(table.contain(arrayName.append(index).toString())){
					arrayName.replace(arrayName.length()-1, arrayName.length(), ":");
				}
				((ArrayTableCreatorData)data).anonymousIndex++;
				
				// create new array table entry for anonymous static array
				BDPLArrayTableEntry entry = new BDPLArrayTableEntry();
				entry.setName(arrayName.toString());
				entry.setType(arrayType);
				entry.setSource(((ASTStaticArrayDef1)ayArrayNode).getSource());
				
				try{
					table.add(arrayName.toString(), entry);
				}
				catch(BDPLArrayException ae){
					throw new VisitorException(ae.getMessage());
				}
				
				// replace the node of explicit array with anonymous array variable node
				ASTArrayVar repNode = new ASTArrayVar(SyntaxTreeBuilderTreeConstants.JJTARRAYVAR);
				ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
				varNode.setName(arrayName.toString());
				repNode.jjtInsertChild(varNode, 0);
				repNode.setName(arrayName.toString());
				
				
				arrayName.delete(0, arrayName.length());
				
				node.jjtReplaceChild(ayArrayNode, repNode);
			}
			
			return data;
		}
		
		@Override
		public Object visit(ASTArrayVar node, Object data)
				throws VisitorException
		{
			/*
			 * in dynamic array declaration
			 */
			
			BDPLArray ret = null;
			// the 1st child of ASTArrayVariable must be ASTVar !!! Pay attention to grammar file
			node.setName(((ASTVar)node.jjtGetChild(0)).getName());
			
			// create dynamic array object here
			String size = node.getSize();
			if(size != null){
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
			 *	INSERT INTO ?x(size) VALUES (checked) (?x, ... ?y)
			 */
			Set<String> realTimeCommonVars = ((ArrayTableCreatorData)data).getRealTimeCommonVars();
			
			StringBuffer sourceText = ((ArrayTableCreatorData)data).getSourceText();
			// every child of ASTDynamicArayDef1 must be ASTVar !!! Pay attention to grammar file
			for(int i = 0; i < node.jjtGetNumChildren(); i++){
				String var = ((ASTVar)node.jjtGetChild(i)).getName();
				if(realTimeCommonVars.contains(var)){
					sourceText.append(var+" ");
				}
				else{
					throw new VisitorException("Selected variable \'"+var+"\' for a dynamic array dose not exist in all possible real time event patterns.");
				}
			}
			node.setSource(sourceText.toString());
			sourceText.delete(0, sourceText.length());
			
			return BDPLArrayType.DYNAMIC_VAR;
		}
		

		@Override
		public Object visit(ASTDynamicArrayDef2 node, Object data)
				throws VisitorException
		{
			/* 
			 *  not used
			 * 
			 * 	INSERT INTO ?x(size) SUB CONSTRUCT QUERY 
			 */
			
			StringBuffer sourceText = ((ArrayTableCreatorData)data).getSourceText();
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
			
			node.setSource(prologText+" "+sourceText.toString());
			sourceText.delete(0, sourceText.length());
			
			return BDPLArrayType.DYNAMIC_QUERY;
		}
		
		@Override
		public Object visit(ASTDynamicArrayDef3 node, Object data)
				throws VisitorException
		{
			/*
			 * 	INSERT INTO ?x(size) VALUES function(?x)
			 */
			Set<String> realTimeCommonVars = ((ArrayTableCreatorData)data).getRealTimeCommonVars();
			
			StringBuffer sourceText = ((ArrayTableCreatorData)data).getSourceText();
			
			String fName = node.jjtGetChild(ASTIRI.class).getValue();
			sourceText.append(fName+" ");
			
			// 2nd child of ASTDynamicArayDef3 must be ASTVar !!! Pay attention to grammar file
			String var = node.jjtGetChild(ASTVar.class).getName();
			if(realTimeCommonVars.contains(var)){
				sourceText.append(var);
			}
			else{
				throw new VisitorException("Selected variable \'"+var+"\' for a dynamic array dose not exist in all possible real time event patterns.");
			}
						
			node.setSource(sourceText.toString());
			sourceText.delete(0, sourceText.length());
			
			return BDPLArrayType.DYNAMIC_VAR_1;
		}
		
		@Override
		public Object visit(ASTStaticArrayDef1 node, Object data)
				throws VisitorException
		{
			/*
			 * 	CONTEXT ?x { (1, 2, 3;) }
			 *  (1, 2, 3;) anonymous array _x()
			 */
			StringBuffer sourceText = ((ArrayTableCreatorData)data).getSourceText();
			
			// between every child node ASTArrayElement should be a ";". Pay attention to grammar file.
			for(Node child : node.jjtGetChildren()){
				child.jjtAccept(this, data);
				sourceText.append(";");
			} 
			
			node.setSource(sourceText.toString());
			sourceText.delete(0, sourceText.length());
			
			
			return BDPLArrayType.STATIC_EXPLICITE;
		}
		
		@Override
		public Object visit(ASTArrayElement node, Object data)
				throws VisitorException
		{	
			StringBuffer sourceText = ((ArrayTableCreatorData)data).getSourceText();
			
			for(Node child : node.jjtGetChildren()){
				if(child instanceof ASTRDFLiteral){
					ASTRDFLiteral rdfLiteral = (ASTRDFLiteral)child;
					sourceText.append("\""+rdfLiteral.getLabel().getValue()+"\"");
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
					
					if(numLiteral.getDatatype() != null){
						sourceText.append("\""+numLiteral.getValue()+"\"");
						sourceText.append("^^"+numLiteral.getDatatype()+" ");
					}
					else{
						sourceText.append(numLiteral.getValue()+" ");
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
			 * 	CONTEXT ?x { SELECT QUERY } 
			 */
			
			StringBuffer sourceText = ((ArrayTableCreatorData)data).getSourceText();
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
			
			node.setSource(prologText+" "+sourceText.toString());
			sourceText.delete(0, sourceText.length());
			
			return BDPLArrayType.STATIC_QUERY;
		}
	}
}
