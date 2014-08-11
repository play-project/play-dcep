/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTA;
import org.openrdf.query.parser.bdpl.ast.ASTB;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTC;
import org.openrdf.query.parser.bdpl.ast.ASTConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTContextClause;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTEventClause;
import org.openrdf.query.parser.bdpl.ast.ASTEventPattern;
import org.openrdf.query.parser.bdpl.ast.ASTNotClause;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.ASTSubBDPLQuery;
import org.openrdf.query.parser.bdpl.ast.ASTTimeBasedEvent;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.Token;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.EPLConstants;

/**
 * Not used
 * 
 * @author ningyuan 
 * 
 * Apr 30, 2014
 *
 */

public class EPLListenerProcessor {
	
	public static String process(ASTOperationContainer qc, String prologText)
			throws MalformedQueryException{
		
		ConstructQueryCreator queryCreator = new ConstructQueryCreator(prologText);
		
		try {
			qc.jjtAccept(queryCreator, null);
			return queryCreator.constructQuery;
			
		} catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class ConstructQueryCreator extends ASTVisitorBase {
		
		String constructQuery = null;
		
		private final String prologText;
		
		public ConstructQueryCreator(String prologText){
			this.prologText = prologText;
		}
		

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
		 * Visited nodes
		 * 
		 */
		
		@Override
		public Object visit(ASTEventPattern node, Object data)
			throws VisitorException
		{
			
			String ret;
			
			// EventPattern = C
			if(node.jjtGetNumChildren() <= 1){
				ret = (String) super.visit(node, data);
				
				if(node.getTop())
				{
					constructQuery = getQuery(prologText, ret);
				}
				
				
				return ret;
			}
			// EventPattern = C (seq C)+
			else{
				List<String> graphs = new ArrayList<String>();
				
				for(Node child : node.jjtGetChildren()){
					graphs.add((String)child.jjtAccept(this, data));
				}
				
				// return the connected graph
				
				ret =  connectGraphs(graphs);
				
				
				if(node.getTop())
				{
					constructQuery = getQuery(prologText, ret);
				}
				/*else{
					ret = String.format(BDPLConstants.SPARQL_CLAUSE, ret);
				}*/
				
				return ret;
			}
			
		}
		
		@Override
		public Object visit(ASTC node, Object data)
				throws VisitorException
		{
			
			// C = B
			if(node.jjtGetNumChildren() <= 1){
				return super.visit(node, data);
			}
			// C = B (or B)+
			else{
				List<String> graphs = new ArrayList<String>();
							
				for(Node child : node.jjtGetChildren()){
					graphs.add((String)child.jjtAccept(this, data));
				}
				
				// return the united graph
				return unionGraphs(graphs);
			}
		}
		
		@Override
		public Object visit(ASTB node, Object data)
				throws VisitorException
		{
			
			// B = A
			if(node.jjtGetNumChildren() <= 1){
				return super.visit(node, data);
			}
			// B = A (and A)+
			else{
				List<String> graphs = new ArrayList<String>();
										
				for(Node child : node.jjtGetChildren()){
					graphs.add((String)child.jjtAccept(this, data));
				}
				
				// return the connected graph
				
				return connectGraphs(graphs);
				
			}
		}
		
		@Override
		public Object visit(ASTA node, Object data)
				throws VisitorException
		{
			return super.visit(node, data);
		}
		
		@Override
		public Object visit(ASTNotClause node, Object data)
				throws VisitorException
		{
			// A -> C and not B
			
			
			// get 3 child terms of the not clause
			List<Node> children = node.jjtGetChildren();
			
			if(children.size() != 3){
				// NotClause must have 3 children. Pay attention to the jjtree file!
				throw new VisitorException("Not Clause dose not have 3 children nodes");
			}
			
			List<String> graphs = new ArrayList<String>();
			
			graphs.add((String) children.get(0).jjtAccept(this, data));
			graphs.add((String) children.get(2).jjtAccept(this, data));
			
			return connectGraphs(graphs);
		}
		
		@Override
		public Object visit(ASTTimeBasedEvent node, Object data)
				throws VisitorException
		{
	
			super.visit(node, data);
			
			String ret = "";
			
			return ret;
		}
		
		@Override
		public Object visit(ASTEventClause node, Object data)
				throws VisitorException
		{
			
			/*
			 * visit child nodes
			 */
			super.visit(node, data);
			
			/*
			 * Get the sparql text of this event 
			 */
			
			StringBuffer eventClauseText = new StringBuffer();
			Token token = node.jjtGetFirstToken();
			for(int i = 0; i < 3; i++){
				token = token.next;
			}
			for(; token != node.jjtGetLastToken(); token = token.next){
				eventClauseText.append(token.image+" ");
			}
			// last token must be }
			
			return eventClauseText.toString();
		}
		
		private String connectGraphs(List<String> graphs){
			StringBuffer ret = new StringBuffer();
			for(int i = 0; i < graphs.size(); i++){
				String graph = graphs.get(i).trim();
				if(graph.length() > 0 && graph.charAt(graph.length()-1) != '.' && graph.charAt(graph.length()-1) != '}'){
					ret.append(graph+". ");
				}
				else{
					ret.append(graph+" ");
				}
			}
			return ret.toString();
		}
		
		private String unionGraphs(List<String> graphs){
			StringBuffer ret = new StringBuffer();
			ret.append(String.format(BDPLConstants.SPARQL_CLAUSE, graphs.get(0))+" ");
			for(int i = 1; i < graphs.size(); i++){
				ret.append(EPLConstants.SPARQL_UNION+" "+String.format(BDPLConstants.SPARQL_CLAUSE, graphs.get(i)+" "));
			}
			
			return String.format(BDPLConstants.SPARQL_CLAUSE, ret.toString());
		}
		
		private String getQuery(String prolog, String graph){
			StringBuffer query = new StringBuffer();
			query.append(prolog+" SELECT * "+ String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, graph));
			return query.toString();
		}
	}
}
