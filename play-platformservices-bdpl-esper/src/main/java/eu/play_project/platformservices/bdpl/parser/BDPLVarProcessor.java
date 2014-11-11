/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.ast.ASTA;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVar;
import org.openrdf.query.parser.bdpl.ast.ASTB;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstruct;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstructTriplesSameSubjectPath;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTBindingsClause;
import org.openrdf.query.parser.bdpl.ast.ASTC;
import org.openrdf.query.parser.bdpl.ast.ASTContextClause;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTEventClause;
import org.openrdf.query.parser.bdpl.ast.ASTEventGraphPattern;
import org.openrdf.query.parser.bdpl.ast.ASTEventPattern;
import org.openrdf.query.parser.bdpl.ast.ASTNotClause;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.ASTRealTimeEventQuery;
import org.openrdf.query.parser.bdpl.ast.ASTSubBDPLQuery;
import org.openrdf.query.parser.bdpl.ast.ASTTimeBasedEvent;
import org.openrdf.query.parser.bdpl.ast.ASTTriplesSameSubjectPath;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.util.BDPLVarTable;

/**
 * A processor of BDPL grammatic tree. gathers information about variables in construct clause, 
 * and common variables in event pattern of real time query.
 * 
 * @author ningyuan 
 * 
 * Jul 27, 2014
 *
 */
public class BDPLVarProcessor {
	
	public static BDPLVarTable process(ASTOperationContainer qc)
			throws MalformedQueryException{
		VarTableCreator vtCreator = new VarTableCreator();
		
		VarTableCreatorData data = new VarTableCreatorData();
		
		try {
			qc.jjtAccept(vtCreator, data);
			
			BDPLVarTable varTable = data.getVarTable();
			//TODO check construct variables are contained in real or historical 
			
			/*Set<String> rcVs = data.getVarTable().getRealTimeCommonVars();
			Set<String> cVs = data.getVarTable().getConstructVars();
			
			for(String cV : cVs){
				if(!rcVs.contains(cV)){
					throw new MalformedQueryException("Construct variable ?"+cV+" dose not appear in common variables of real time event pattern");
				}
			}*/
				// for test
				System.out.println("\nBDPLVarProcessor construct variables: ");
				for(String var : varTable.getConstructVars()){
					System.out.print(var+"   ");
				}
				System.out.println("\nBDPLVarProcessor common variables: ");
				for(String var : varTable.getRealTimeCommonVars()){
					System.out.print(var+"   ");
				}
			
			return varTable;
			
		} catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class VarTableCreatorData{
		
		// indicate whether variable is in triple block
		private boolean inTriple = false;
		
		/*
		 * 0: initiate
		 * 1: in bdpl construct clause
		 * 2: in real time event query (not in bdpl sub query)
		 */
		private int state = 0;
		
		private BDPLVarTable varTable = new BDPLVarTable();
		
		/*
		 * temporary container of all names of variable in one event clause
		 */
		private Set<String> varInEventClause = new HashSet<String>();
		
		
		private boolean isInTriple() {
			return this.inTriple;
		}

		private void setInTriple(boolean inTriple) {
			this.inTriple = inTriple;
		}
		
		private void clearVarInEventClause() {
			this.varInEventClause = new HashSet<String>();
		}

		private Set<String> getVarInEventClause() {
			return this.varInEventClause;
		}

		private int getState() {
			return this.state;
		}

		private void setState(int state) {
			this.state = state;
		}

		private BDPLVarTable getVarTable() {
			return this.varTable;
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
		public Object visit(ASTContextClause node, Object data)
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
		
		// ignore all array variables
		@Override
		public Object visit(ASTArrayVar node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		// SubBDPLQuery is skipped ( BDPLWhereClause in SubBDPLQuery )
		@Override
		public Object visit(ASTSubBDPLQuery node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		
		
		/*
		 * visited nodes
		 * 
		 */	
		@Override
		public Object visit(ASTBDPLConstruct node, Object data)
				throws VisitorException
		{
			int s = ((VarTableCreatorData)data).state;
			((VarTableCreatorData)data).setState(1);
				
			node.childrenAccept(this, data);
				
			((VarTableCreatorData)data).setState(s);
			
			return data;
		}
		
		
		@Override
		public Object visit(ASTRealTimeEventQuery node, Object data)
				throws VisitorException
		{
			int s = ((VarTableCreatorData)data).state;
			((VarTableCreatorData)data).setState(2);
				
			node.childrenAccept(this, data);
				
			((VarTableCreatorData)data).setState(s);
			
			return data;
		}
		
		@Override
		public Object visit(ASTEventPattern node, Object data)
				throws VisitorException
		{
			Set<String> rtvs;
			if(node.getTop()){
				rtvs = ((VarTableCreatorData)data).getVarTable().getRealTimeCommonVars();
				/*
				 *  pattern [C -> ... -> C]
				 */
				for(Node child : node.jjtGetChildren()){
					Set<String> rtvsc = (Set<String>)child.jjtAccept(this, data);
					for(String rtv : rtvsc){
						rtvs.add(rtv);
					}
				}
				
				return data;
			}
			else{
				/*
				 * ( C -> ... -> C )
				 */
				Node firstChild = node.jjtGetChild(0);
				rtvs = (Set<String>)firstChild.jjtAccept(this, data);
				for(int i = 1; i < node.jjtGetNumChildren(); i++){
					Set<String> rtvsc = (Set<String>)node.jjtGetChild(i).jjtAccept(this, data);
					for(String rtv : rtvsc){
						rtvs.add(rtv);
					}
				}
				
				return rtvs;
			}
		}
		
		@Override
		public Object visit(ASTC node, Object data)
				throws VisitorException
		{	
			List<Set<String>> orRtvs = new ArrayList<Set<String>>();
			
			/*
			 * B or ... or B
			 */
			for(Node child : node.jjtGetChildren()){
				orRtvs.add((Set<String>)child.jjtAccept(this, data));
			}
			
			return getCommenRealTimeVariables(orRtvs);
			
		}
		
		
		@Override
		public Object visit(ASTB node, Object data)
				throws VisitorException
		{	
			
			/*
			 * A and ... and A
			 */
			Node firstChild = node.jjtGetChild(0);
			Set<String> rtvs = (Set<String>)firstChild.jjtAccept(this, data);

			for(int i = 1; i < node.jjtGetNumChildren(); i++){
				Set<String> rtvsc = (Set<String>)node.jjtGetChild(i).jjtAccept(this, data);
				for(String rtv : rtvsc){
					rtvs.add(rtv);
				}
			}
			
			return rtvs;
		}
		
		@Override
		public Object visit(ASTA node, Object data)
				throws VisitorException
		{	
			/*
			 * NOT CLAUSE, EVENT CLAUSE, TIME BASED EVENT, (EVENT PATTERN)
			 */
		
			Node firstChild = node.jjtGetChild(0);
			
			Set<String> rtvs = (Set<String>)firstChild.jjtAccept(this, data);
			
			return rtvs;
			
		}
		
		@Override
		public Object visit(ASTNotClause node, Object data)
				throws VisitorException
		{
			Node firstChild = node.jjtGetChild(0);
			Node lastChild = node.jjtGetChild(2);
			
			Set<String> rtvs = (Set<String>)firstChild.jjtAccept(this, data);
			Set<String> rtvsc = (Set<String>)lastChild.jjtAccept(this, data);
			
			for(String rtv : rtvsc){
				rtvs.add(rtv);
			}
			
			return rtvs;
		}
		
		@Override
		public Object visit(ASTTimeBasedEvent node, Object data)
				throws VisitorException
		{
			return new HashSet<String>();
		}
		
		@Override
		public Object visit(ASTEventClause node, Object data)
				throws VisitorException
		{
			
			Node egpn = node.jjtGetChild(ASTEventGraphPattern.class);
			egpn.jjtAccept(this, data);
			
			Set<String> rtvs = ((VarTableCreatorData)data).getVarInEventClause();
			((VarTableCreatorData)data).clearVarInEventClause();
			
			return rtvs;
		}
		
		@Override
		public Object visit(ASTBDPLConstructTriplesSameSubjectPath node, Object data)
				throws VisitorException
		{
			((VarTableCreatorData)data).setInTriple(true);
			node.childrenAccept(this, data);
			((VarTableCreatorData)data).setInTriple(false);
			
			return data;
		}
		
		@Override
		public Object visit(ASTTriplesSameSubjectPath node, Object data)
				throws VisitorException
		{
			((VarTableCreatorData)data).setInTriple(true);
			node.childrenAccept(this, data);
			((VarTableCreatorData)data).setInTriple(false);
			
			return data;
		}
		
		
		@Override
		public Object visit(ASTVar node, Object data)
				throws VisitorException
		{
			if(((VarTableCreatorData)data).isInTriple()){
				int s = ((VarTableCreatorData)data).getState();
				
				switch(s){
					// var in triple block of bdpl construct clause 
					case 1:{
						((VarTableCreatorData)data).getVarTable().getConstructVars().add(node.getName());
						break;
					}
					// var in triple block of event real time event
					case 2:{
						((VarTableCreatorData)data).getVarInEventClause().add(node.getName());
						break;
					}
					/*default:{
						System.out.println(s+" "+node.getName());
					}*/
				}
				
				return data;
			}

				
			return data;
			
		}
		
		//TODO: var in historical part 
		
		private Set<String> getCommenRealTimeVariables(List<Set<String>> orRtvs){
			Set<String> ret = new HashSet<String>();
			
			Set<String> smallestSet = orRtvs.get(0);
			
			for(int i = 1; i < orRtvs.size(); i++){
				if(smallestSet.size() > orRtvs.get(i).size())
					smallestSet = orRtvs.get(i);
			}
			
			if(smallestSet.size() > 0){
				orRtvs.remove(smallestSet);
				
				for(String rtv : smallestSet){
					boolean common = true;
					for(int i = 0; i < orRtvs.size(); i++){
						Set<String> otherSet = orRtvs.get(i);
						
						if(!otherSet.contains(rtv)){
							common = false;
							break;
						}
					}
					
					if(common){
						ret.add(rtv);
					}
				}
			}
			
			return ret;
		}
	}
}
