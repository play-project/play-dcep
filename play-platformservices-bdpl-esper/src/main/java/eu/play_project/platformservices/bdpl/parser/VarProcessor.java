/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVariable;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTStaticArrayDef1;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.ArrayDef;
import org.openrdf.query.parser.bdpl.ast.Node;
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
		
		ArrayTable varTable = new ArrayTable();
		
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
				
				ArrayTable table = (ArrayTable)data;
				table.add(arrayValNode.getName(), entry);
				
				// XXX check super.visit()
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
			super.visit(node, data);
			
			BDPLArray ret;
			// the 1st child of ASTArrayVariable must be ASTVar !!! Pay attention to grammar file
			node.setName(((ASTVar)node.jjtGetChild(0)).getName());
			
			
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
			super.visit(node, data);
			node.setSource("d1");
			return true;
		}
		
		@Override
		public Object visit(ASTStaticArrayDef1 node, Object data)
				throws VisitorException
		{
			super.visit(node, data);
			node.setSource("s1");
			return false;
		}
	}
}
