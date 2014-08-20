/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.PrefixDeclProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.TupleExprBuilder;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTArrayFilter;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTEventClause;
import org.openrdf.query.parser.bdpl.ast.ASTEventGraphPattern;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SimpleNode;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.Token;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;





/**
 * check bdpl specific syntax and parse prolog text
 * 
 * @author ningyuan 
 * 
 * Jun 30, 2014
 *
 */
public class BDPLSyntaxCheckProcessor {
	
	public static String process(ASTOperationContainer qc)
			throws MalformedQueryException{
		
		BDPLSyntaxChecker syntaxChecker = new BDPLSyntaxChecker();
		
		BDPLSyntaxCheckerData data = new BDPLSyntaxCheckerData();
		
		try {
			qc.jjtAccept(syntaxChecker, data);
			return ((BDPLSyntaxCheckerData)data).getPrologText().toString();
			
		} catch (VisitorException e) {
			e.printStackTrace();
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class BDPLSyntaxCheckerData{
		
		
		private StringBuffer prologText = new StringBuffer();
		
		/*
		 * temp variable for saving the Sparql text of each event
		 */
		private StringBuffer eventClauseText = new StringBuffer();
		
		public StringBuffer getEventClauseText(){
			return eventClauseText;
		}
		
		public StringBuffer getPrologText(){
			return prologText;
		}
	}

	private static class BDPLSyntaxChecker extends ASTVisitorBase {
		
		
		
		/*
		 * visited nodes
		 * 
		 */
		
		@Override
		public Object visit(ASTPrefixDecl node, Object data)
				throws VisitorException
		{
			
			/*
			 * Get the prefix in prolog  
			 */
			StringBuffer prologText = ((BDPLSyntaxCheckerData)data).getPrologText();
			Token token = node.jjtGetFirstToken();
			
			boolean nullToken = false; 
			for(; token != node.jjtGetLastToken(); ){
				if(token != null){
					prologText.append(token.image+" ");
					token = token.next;
				}
				else{
					nullToken = true;
					break;
				}
			}
			if(!nullToken && token != null){
				prologText.append(token.image+" ");
			}
				
			return data;
		}
		
		@Override
		public Object visit(ASTBaseDecl node, Object data)
				throws VisitorException
		{
			
			/*
			 * Get the base in prolog  
			 */
			StringBuffer prologText = ((BDPLSyntaxCheckerData)data).getPrologText();
			Token token = node.jjtGetFirstToken();
			
			boolean nullToken = false; 
			for(; token != node.jjtGetLastToken(); ){
				if(token != null){
					prologText.append(token.image+" ");
					token = token.next;
				}
				else{
					nullToken = true;
					break;
				}
			}
			if(!nullToken && token != null){
				prologText.append(token.image+" ");
			}
				
			return data;
		}
		
		/*@Override
		public Object visit(ASTEventClause node, Object data)
				throws VisitorException
		{
			
			
			 * visit child nodes
			 
			super.visit(node, data);
			
			
			  Get the sparql text of this event !!! pay attention to the grammar  
			 
			StringBuffer prologText = ((BDPLSyntaxCheckerData)data).getPrologText();
			StringBuffer eventClauseText = ((BDPLSyntaxCheckerData)data).getEventClauseText();
			Token token = node.jjtGetFirstToken();
			for(int i = 0; i < 3; i++){
				token = token.next;
			}
			for(; token != node.jjtGetLastToken(); token = token.next){
				
				eventClauseText.append(token.image+" ");
			}
			// last token must be }
			
			try{
				checkEventClause(prologText.toString(), eventClauseText.toString());
				eventClauseText.delete(0, eventClauseText.length());
			}
			catch(VisitorException e){
				throw new VisitorException(e.getMessage());
			}
			
			return data;
		}*/
		
		@Override
		public Object visit(ASTEventGraphPattern node, Object data)
				throws VisitorException
		{
			StringBuffer prologText = ((BDPLSyntaxCheckerData)data).getPrologText();
			StringBuffer eventClauseText = ((BDPLSyntaxCheckerData)data).getEventClauseText();
			Token token = node.jjtGetFirstToken();
			
			int state = 0, parath = 0;
			for(; token != node.jjtGetLastToken(); token = token.next){
				switch(state){
					// before array filter
					case 0:{
						if(token.image.equalsIgnoreCase("arrayfilter")){
							state = 1;
						}
						else{
							eventClauseText.append(token.image+" ");
						}
						break;
					}
					// in array filter
					case 1:{
						if(token.image.equalsIgnoreCase("(")){
							parath++;
						}
						else if(token.image.equalsIgnoreCase(")")){
							if(parath > 0){
								parath--;
							}
							else{
								throw new VisitorException("TODO");
							}
							
							if(parath == 0){
								state = 2;
							}
						}
			
						break;
					}
					// after array filter
					case 2:{
						if(token.image.equalsIgnoreCase(" ") || token.image.equalsIgnoreCase("\n") || token.image.equalsIgnoreCase("\r") 
								|| token.image.equalsIgnoreCase("\t") || token.image.equalsIgnoreCase("\f")){
							eventClauseText.append(token.image+" ");
						}
						else if(token.image.equalsIgnoreCase(".")){
							state = 0;
						}
						else if(token.image.equalsIgnoreCase("arrayfilter")){
							state = 1;
						}
						else{
							eventClauseText.append(token.image+" ");
							state = 0;
						}
						break;
					}
				}
			}
			if(state != 1){
				eventClauseText.append(token.image+" ");
			}
			
			
			try{
					//System.out.println(eventClauseText.toString());
				checkEventClause(prologText.toString(), eventClauseText.toString());
					
				eventClauseText.delete(0, eventClauseText.length());
			}
			catch(VisitorException e){
				throw new VisitorException(e.getMessage());
			}
			
			return data;
		}
		
		
		private void checkEventClause(String prolog, String text) throws VisitorException{
			try{
				
				ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(prolog+String.format(BDPLConstants.SPARQL_ASK_QUERY, text));
				StringEscapesProcessor.process(qc);
				BaseDeclProcessor.process(qc, null);
				PrefixDeclProcessor.process(qc);
				WildcardProjectionProcessor.process(qc);
				BlankNodeVarProcessor.process(qc);
				TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(new ValueFactoryImpl());
				TupleExpr tupleExpr = (TupleExpr)qc.jjtAccept(tupleExprBuilder, null);
				
				List<StatementPattern> statementPatterns = StatementPatternCollector.process(tupleExpr);
				boolean hasType = false, hasStream = false;
				for(StatementPattern sp : statementPatterns){
					if(!hasType){
						if(sp.getPredicateVar().getValue().equals(RDF.TYPE)){
							Value val = sp.getObjectVar().getValue();
							if(val != null){
								
								hasType = true;
							}
							else{
								throw new VisitorException("Object of rdf:type has no value");
							}
						}
					}
					if(!hasStream){
						if(sp.getPredicateVar().getValue().stringValue().equals(BDPLConstants.URI_STREAM)){
							Value val = sp.getObjectVar().getValue();
							if(val != null){
								
								hasStream = true;
							}
							else{
								throw new VisitorException("Object of "+BDPLConstants.URI_STREAM+" has no value");
							}
							
						}
						
					}
				}
				
				if(!hasType){
					throw new VisitorException("Event clause contains no rdf:type");
				}
				else if(!hasStream){
					throw new VisitorException("Event clause contains no "+BDPLConstants.URI_STREAM);
				}
			}
			catch (ParseException e) {
				throw new VisitorException(e.getMessage());
			}
			catch (TokenMgrError e) {
				throw new VisitorException(e.getMessage());
			}
			catch (MalformedQueryException e){
				throw new VisitorException(e.getMessage());
			}
		}
	}
}
