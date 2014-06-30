/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVariable;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef2;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTStaticArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTStaticArrayDef2;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.ArrayDef;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.Token;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.util.ArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.ArrayTableEntry;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;



/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class VarProcessor {
	
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
		
		@Override
		public Object visit(ASTArrayDecl node, Object data)
				throws VisitorException
		{
			// ASTArrayDecl must have one ASTArrayVariable node. !!! Pay attention to grammar file
			ASTArrayVariable arrayValNode = node.jjtGetChild(ASTArrayVariable.class);
			BDPLArray array = (BDPLArray)arrayValNode.jjtAccept(this, data);
			
			// the 1st child of ASTArrayDecl must be a ArrayDef. !!! Pay attention to grammar file
			// a ArrayDef has a 'source' property
			Node arrayDefNode = node.jjtGetChild(0);
			boolean dynamic = (boolean)arrayDefNode.jjtAccept(this, data);
			
			if(dynamic){
				if(arrayValNode.getSize() == null){
					throw new VisitorException("A size must be declared for a dynamic array.");
				}
			}
			else{
				if(arrayValNode.getSize() != null){
					throw new VisitorException("A size should not be declared for a static array.");
				}
			}
			
			// create new array table entry
			ArrayTableEntry entry = new ArrayTableEntry();
			entry.setArray(array);
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
			super.visit(node, null);
			
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
			 *	(checked) ?x AS ?x() 
			 */
			super.visit(node, null);
			node.setSource("dynamic definition");
			return true;
		}
		
		@Override
		public Object visit(ASTDynamicArrayDef2 node, Object data)
				throws VisitorException
		{
			/*
			 * 	{ SUB CONSTRUCT QUERY } AS ?x()
			 */
			super.visit(node, null);
			
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
			
			return true;
		}
		
		@Override
		public Object visit(ASTStaticArrayDef1 node, Object data)
				throws VisitorException
		{
			/*
			 * 	(1 2 3) AS ?x()
			 */
			super.visit(node, null);
			
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
			
			return false;
		}
		
		@Override
		public Object visit(ASTStaticArrayDef2 node, Object data)
				throws VisitorException
		{
			/*
			 * 	{ SELECT QUERY } AS ?x()
			 */
			super.visit(node, null);
			
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
			
			return false;
		}
	}
}
