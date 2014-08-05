/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.PrefixDeclProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.TupleExprBuilder;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
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
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.ASTRealTimeEventQuery;
import org.openrdf.query.parser.bdpl.ast.ASTString;
import org.openrdf.query.parser.bdpl.ast.ASTSubBDPLQuery;
import org.openrdf.query.parser.bdpl.ast.ASTTimeBasedEvent;
import org.openrdf.query.parser.bdpl.ast.ASTWindowClause;
import org.openrdf.query.parser.bdpl.ast.ASTWindowDecl;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.Token;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;
import org.openrdf.query.parser.bdpl.ast.VisitorException;
import org.openrdf.query.MalformedQueryException;

import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.EPLConstants;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.EPLTranslateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.EPLTranslateUtil;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.EPLTranslationData;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.IEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.NotEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.NotTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.OrClause;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.SeqClause;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.Term;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TimeDelayEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TimeDelayTable;



/**
 * Processes the part of real time query, and translates it into a EPL for Esper.
 * 
 * @author ningyuan
 *
 */
public class EPLTranslationProcessor {
	
	public static EPLTranslationData process(ASTOperationContainer qc, String prologText)
			throws MalformedQueryException{
		EPLTranslator translator = new EPLTranslator();
		
		EPLTranslatorData data = new EPLTranslatorData(prologText);
		
		try {
			qc.jjtAccept(translator, data);
			
			EPLTranslationData ret = new EPLTranslationData(translator.epl, translator.injectParams);
			
			return ret;
			
		} catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage());
		}
	}
	
	private static class EPLTranslatorData{
		
		private NotTable notTable = new NotTable();
		
		/*
		 * temp variable for saving the Sparql text of each event
		 */
		private StringBuffer eventClauseText = new StringBuffer();
		
		private long windowDuration = -1l;
		
		private String windowType = null;
		
		private String prologText;
		
		public EPLTranslatorData(String p){
			prologText = p;
		}
		
		public void setWindowParam(long d, String t){
			windowDuration = d;
			windowType = t;
		}
		
		public long getWindowDuration(){
			return windowDuration;
		}
		
		public String getWindowType(){
			return windowType;
		}
		
		public NotTable getNotTable(){
			return notTable;
		}
		
		public StringBuffer getEventClauseText(){
			return eventClauseText;
		}
		
		public String getPrologText(){
			return prologText;
		}
	}
	
	private static class EPLTranslator extends ASTVisitorBase {
		
		private static int MAX_NUM_SEQ_CLAUSE = 24;
		
		String epl = null;
		
		List<Integer> injectParams = new ArrayList<Integer>();
		
		//List<String> matchedPatternSparql = new ArrayList<String>();
		
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
		 * visited nodes
		 */
		
		@Override
		public Object visit(ASTRealTimeEventQuery node, Object data)
				throws VisitorException
		{
			OrClause ret = (OrClause)super.visit(node, data);
			
			try{
				epl = getEPL(ret, (EPLTranslatorData)data);
			}catch (EPLTranslateException e) {
				throw new VisitorException(e.getMessage());
			}
			
			return ret;
		}
		
		@Override
		public Object visit(ASTWindowClause node, Object data)
				throws VisitorException
		{
			OrClause ret = null;
			
			for(Node child : node.jjtGetChildren()){
				if(child instanceof ASTEventPattern){
					ret = (OrClause)child.jjtAccept(this, data);
				}
				//TODO filter
				else{
					child.jjtAccept(this, data);
				}
			}
			if(ret == null){
				throw new VisitorException("WindowClause dose not have an EventPattern");
			}
			
			return ret;
		}
		
		@Override
		public Object visit(ASTWindowDecl node, Object data)
				throws VisitorException
		{
			Object ret = super.visit(node, data);
			
			ASTString duration = node.jjtGetChild(ASTString.class);
			if(duration == null){
				throw new VisitorException("WindowDecl dose not have a duration string");
			}
			
			try {
				((EPLTranslatorData)data).setWindowParam(EPLTranslateUtil.getDurationInSec(duration.getValue()), node.getType());
			} catch (EPLTranslateException e) {
				throw new VisitorException("Time delay format exception: "+duration.getValue());
			}
			
			return ret;
		}
		
		@Override
		public Object visit(ASTEventPattern node, Object data)
			throws VisitorException
		{
			
			// EventPattern = C
			if(node.jjtGetNumChildren() <= 1){
				
				return super.visit(node, data);
			}
			// EventPattern = C (seq C)+
			else{
				List<OrClause> expressions = new ArrayList<OrClause>();
				
				for(Node child : node.jjtGetChildren()){
					expressions.add((OrClause)child.jjtAccept(this, data));
				}
				
				// return the united expression
				try {
					return  unionSeqExpression(expressions);
				} catch (EPLTranslateException e) {
					throw new VisitorException(e.getMessage());
				}
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
				List<OrClause> expansions = new ArrayList<OrClause>();
							
				for(Node child : node.jjtGetChildren()){
					expansions.add((OrClause)child.jjtAccept(this, data));
				}
				
				// return the united expression
				return unionOrExpression(expansions);
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
				List<OrClause> expansions = new ArrayList<OrClause>();
										
				for(Node child : node.jjtGetChildren()){
					expansions.add((OrClause)child.jjtAccept(this, data));
				}
				
				// return the united expression
				try {
					return unionAndExpression(expansions);
				} catch (EPLTranslateException e) {
					throw new VisitorException(e.getMessage());
				}
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
			OrClause expression = null;
			
			EPLTranslatorData etd = (EPLTranslatorData) data;
			NotTable notTable = etd.getNotTable();
			
			// get 3 child terms of the not clause
			
			List<Node> children = node.jjtGetChildren();
			
			if(children.size() != 3){
				// NotClause must have 3 children. Pay attention to the jjtree file!
				throw new VisitorException("Not Clause dose not have 3 children nodes");
			}
			
			Term [] notTerms = new Term [3];
			for(int i = 0; i < children.size(); i++){
				expression = (OrClause) children.get(i).jjtAccept(this, data);
				notTerms[i] = expression.getSeqClauses().get(0).getTerms().get(0);
			}
			
			// B is time delay
			if(EPLTranslateUtil.getTermType(notTerms[1]) == EPLTranslateUtil.TERM_TIME){
				// C is time delay
				if(EPLTranslateUtil.getTermType(notTerms[2]) == EPLTranslateUtil.TERM_TIME){
					// B is longer than C
					if(notTerms[1].getDuration() > notTerms[2].getDuration()){
						// A is time delay
						// T -> T : T 	time delay table is null
						if(EPLTranslateUtil.getTermType(notTerms[0]) == EPLTranslateUtil.TERM_TIME){
							notTerms[0].setDuration(notTerms[0].getDuration()+notTerms[2].getDuration());
							
							List<Term> seq = expression.getSeqClauses().get(0).getTerms();
							seq.clear();
							seq.add(notTerms[0]);
							
							return expression;
						}
						// A is event
						// A -> T	set time delay table
						else{
							TimeDelayTable tdTable = new TimeDelayTable();
							List<TimeDelayEntry> entries = tdTable.getEntries();
							entries.add(new TimeDelayEntry(notTerms[0], null, notTerms[2].getDuration()));
							
							expression.getSeqClauses().get(0).setTdTable(tdTable);
							
							List<Term> seq = expression.getSeqClauses().get(0).getTerms();
							seq.clear();
							seq.add(notTerms[0]);
							
							return expression;
						}
					}
					// B is not longer than C
					else{
						throw new VisitorException("Not time delay is not longer than waiting time delay");
					}
				}
				// C is event
				// * -> C and not T
				else{
					
					NotEntry entry = new NotEntry(notTerms[0], notTerms[1], notTerms[2]);
					notTable.addEntry(entry);
					
					// time delay table
					expression.getSeqClauses().get(0).setTdTable(new TimeDelayTable());
					
					List<Term> seq = expression.getSeqClauses().get(0).getTerms();
					seq.clear();
					// fixed time delay, when A is a time delay
					seq.add(notTerms[0]);
					seq.add(notTerms[2]);
					
					return expression;
				}
			}
			// B is event
			// A, C could be time delay and are kept in SEQ CLAUSE, so that no AND transformation is carried out
			// * -> * and not B
			else{
				
				NotEntry entry = new NotEntry(notTerms[0], notTerms[1], notTerms[2]);
				notTable.addEntry(entry);
				
				// time delay table
				expression.getSeqClauses().get(0).setTdTable(new TimeDelayTable());
				
				List<Term> seq = expression.getSeqClauses().get(0).getTerms();
				seq.clear();
				// fixed time delay, when either A or C is a time delay
				seq.add(notTerms[0]);
				seq.add(notTerms[2]);
				
				return expression;
			}
		}
		
		@Override
		public Object visit(ASTTimeBasedEvent node, Object data)
				throws VisitorException
		{
			
			super.visit(node, data);
			
			// ASTTimeBasedEvent must have one ASTString node. !!! Pay attention to grammar file
			ASTString durationString = node.jjtGetChild(ASTString.class);
			if(durationString == null){
				throw new VisitorException("Time delay dose not have a duration string");
			}
			
			/* 
			 * create the new Term of the time delay, time delay table is null
			 */
			Term term = new Term(EPLConstants.TIMER_INTERVAL_NAME);
			try {
				term.setDuration(EPLTranslateUtil.getDurationInSec(durationString.getValue()));
			} catch (EPLTranslateException e) {
				throw new VisitorException("Time delay format exception: "+durationString.getValue());
			}
			
			SeqClause seq = new SeqClause();
			seq.addTerm(term);
			
			OrClause expression = new OrClause();
			expression.addSeqClause(seq);
			
			return expression;
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
			 * Get the sparql text of this event, !!! pay attention to the grammar  
			 */
			String prologText = ((EPLTranslatorData)data).getPrologText();
			StringBuffer eventClauseText = ((EPLTranslatorData)data).getEventClauseText();
			Token token = node.jjtGetFirstToken();
			for(int i = 0; i < 3; i++){
				token = token.next;
			}
			for(; token != node.jjtGetLastToken(); token = token.next){
				eventClauseText.append(token.image+" ");
			}
			// last token must be }
			
			/*
			 * Create the term of this event, time delay table is null
			 */
			OrClause expression;
			try{
				
				String [] pro = getEventProperties(prologText, eventClauseText.toString());
				Term term = new Term(pro[0], pro[1]);
				term.setSparqlText(processSparqlOfEvent(eventClauseText.toString()));
				eventClauseText.delete(0, eventClauseText.length());
				
				SeqClause seq = new SeqClause();
				seq.addTerm(term);
				
				expression = new OrClause();
				expression.addSeqClause(seq);
			}
			catch(EPLTranslateException e){
				throw new VisitorException(e.getMessage());
			}
			
			return expression;
		}
		
		//TODO
		/*
		 * process Sparql query in epl
		 */
		private String processSparqlOfEvent(String s){
			String ret = s.replace("\"", "\'");
			return ret;
		}
		
		/*
		 * 
		 */
		private String[] getEventProperties(String prolog, String sparql) throws EPLTranslateException{
			
			try{
				String [] ret = new String[2];
				
				ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(prolog+String.format(BDPLConstants.SPARQL_ASK_QUERY, sparql));
				StringEscapesProcessor.process(qc);
				BaseDeclProcessor.process(qc, null);
				PrefixDeclProcessor.process(qc);
				WildcardProjectionProcessor.process(qc);
				BlankNodeVarProcessor.process(qc);
				TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(new ValueFactoryImpl());
				TupleExpr tupleExpr = (TupleExpr)qc.jjtAccept(tupleExprBuilder, null);
				
				List<StatementPattern> statementPatterns = StatementPatternCollector.process(tupleExpr);
				
				for(StatementPattern sp : statementPatterns){
					// return the first rdf:type, syntax check is executed by bdpl parser
					if(sp.getPredicateVar().getValue().equals(RDF.TYPE)){
						
						Var subject =sp.getSubjectVar();
						Value object = sp.getObjectVar().getValue();
						
						ret[0] = subject.getName();
						ret[1] = object.stringValue().replaceAll("[^a-zA-Z0-9]", "");
						break;
					}
				}
				
				return ret;
				
			}
			catch (VisitorException e){
				throw new EPLTranslateException(e.getMessage());
			}
			catch (ParseException e) {
				throw new EPLTranslateException(e.getMessage());
			}
			catch (TokenMgrError e) {
				throw new EPLTranslateException(e.getMessage());
			}
			catch (MalformedQueryException e){
				throw new EPLTranslateException(e.getMessage());
			}
			
		}
		
		
		
		
		
		/*
		 * Standard form of the expression (OrClause):
		 * 
		 * Exp = ((TERM (seq TERM)*:=SEQ CLAUSE) or SEQ CLAUSE)*:=OR CLAUSE)
		 */
		
		
		
		/*
		 * Exp = Exp (or Exp)+
		 * 
		 * United OR connected expressions into one expression
		 * 
		 * @param expressions must not be null and must have at least one element
		 */
		private OrClause unionOrExpression(List<OrClause> expressions){
			OrClause ret = expressions.get(0);
			
			for(int i = 1; i < expressions.size(); i++){
				OrClause expression = expressions.get(i);
				List<SeqClause> seqcs = expression.getSeqClauses();
				
				for(int j = 0; j < seqcs.size(); j++){
					ret.addSeqClause(seqcs.get(j));
				}
			}
			
			return ret;
		}
		

		/*
		 * Exp = Exp (seq Exp)+
		 * 
		 * United SEQ connected expressions into one expression
		 * 
		 * @param expressions 
		 * @return united expression
		 */
		private OrClause unionSeqExpression(List<OrClause> expressions) throws EPLTranslateException{
			
			return combineSeqClause(expressions, 1);
		
		}
		
		/*
		 * Exp = Exp (and Exp)+
		 * 
		 * United AND connected expressions into one expression
		 */
		private OrClause unionAndExpression(List<OrClause> expansions) throws EPLTranslateException{
			
			return combineSeqClause(expansions, 2);
			
		}
		
		/*
		 * (SEQ CLAUSE (op SEQ CLAUSE)*):=combination (or (SEQ CLAUSE (op SEQ CLAUSE)*))* = ( SEQ CLAUSE (or SEQ CLAUSE)*):=exp (op (SEQ CLAUSE (or SEQ CLAUSE)*))*
		 * 
		 * 
		 * Find every combination composed of one SeqClause from each expression. Expand every combination and return the result expression connected
		 * by every combination.
		 * 
		 * @param exps list of expressions
		 * @param type the operator connecting expressions. 1 SEQ	2 AND
		 * @return expression in standard form connected by every connection
		 */
		private OrClause combineSeqClause(List<OrClause> exps, int type) throws EPLTranslateException{
			OrClause ret;
			
			int size = exps.size();
			if(size > 0){
				ret = new OrClause();
				List<SeqClause> combination = new ArrayList<SeqClause>();
				
				// a stack of the index of current SEQ CLAUSE of each expression
				int [] stack = new int [size];
				// a pointer to current expression from which a SEQ CLAUSE will be chosen
				int pointer = 0;
				stack[0] = 0;
				
				while(true){
					
					if(pointer < 0){
						break;
					}
					// an SEQ CLAUSE from the last expression is chosen, one combination is made
					// pointer goes back to the previous expression
					else if(pointer >= size){
						switch(type){
							case 1:{
								// SEQ CLAUSE = (SEQ CLAUSE (seq SEQ CLAUSE)*):= combination
								expand1(combination, ret);
								break;
							}
							case 2:{
								// Exp = (SEQ CLAUSE (and SEQ CLAUSE)*):= combination
								expand2(combination, ret);
								break;
							}
						}
						
						// remove the last SEQ CLAUSE
						combination.remove(combination.size()-1);
						pointer --;
					}
					// a further SEQ CLAUSE will be chosen here
					else{
						int index = stack[pointer];
						List<SeqClause> seqcs = exps.get(pointer).getSeqClauses();
						
						// chose a SEQ CLAUSE from this expression
						// pointer goes to the next expression
						if(index < seqcs.size()){
							combination.add(seqcs.get(index));
							stack[pointer] = index + 1;
							pointer++;
							
						}
						// all SEQ CLAUSE from this expression are chosen
						// pointer goes back to the previous expression
						else{
							if(pointer > 0){
								stack[pointer] = 0;
								combination.remove(combination.size()-1);
							}
							pointer --;		
						}
						
					}
				}
				return ret;
			}
			else{
				throw new EPLTranslateException("No expressions for combination");
			}
		}
		
		
		/*
		 * SEQ CLAUSE = (SEQ CLAUSE (seq SEQ CLAUSE)*):= combination
		 * 
		 * 
		 * 
		 * @param combi only used for passing data, the content must not be changed and must not be null
		 * @param result used for save end expression connected by every expanded combination
		 */
		private void expand1(List<SeqClause> combination, OrClause result) throws EPLTranslateException{
			
			// create new result SEQ CLAUSE and its time delay table
			SeqClause resultSeq = new SeqClause();
			List<Term> resultTerms = resultSeq.getTerms();
			TimeDelayTable resultTDTable = new TimeDelayTable();
			resultSeq.setTdTable(resultTDTable);
			
			Term endEvent = null;
			for(int i = 0; i < combination.size(); i++){
				
				// set the end event from last SEQ CLAUSE
				if(resultTerms.size() > 0){
					endEvent = resultTerms.get(resultTerms.size()-1);
				}
				
				SeqClause currentSeq = combination.get(i);
				List<Term> currentTerms = currentSeq.getTerms();
				
				// initiate time delay table of SEQ CLAUSE with only one term
				if(currentSeq.getTdTable() == null){
					initTimeDelayTable(currentSeq);
				}
				
				// copy time delay table of the current term for further connection
				TimeDelayTable currentTDTable = new TimeDelayTable();
				List<TimeDelayEntry> currentEntries = currentSeq.getTdTable().getEntries();
				for(int j = 0; j < currentEntries.size(); j++){
					TimeDelayEntry currentEntry = currentEntries.get(j);
					currentTDTable.getEntries().add(new TimeDelayEntry(currentEntry.getStart(), currentEntry.getEnd(), currentEntry.getDuration()));
				}
					
					
					// for test
					System.out.println("\nSEQ Connected");
					for(int j = 0; j < resultTerms.size(); j++){
						System.out.print(resultTerms.get(j).getName()+" ");
					}
					System.out.print("+ ");
					for(int j = 0; j < currentTerms.size(); j++){
						System.out.print(currentTerms.get(j).getName()+" ");
					}
					System.out.println();
				
				sequenceSeqClauses(endEvent, resultTerms, resultTDTable, currentTerms, currentTDTable);
					
					
					// for test
					List<TimeDelayEntry> temp = resultTDTable.getEntries();
					for(int j = 0; j < temp.size(); j++){
						TimeDelayEntry en = temp.get(j);
						String s = "null", e = "null";
						if(en.getStart() != null)
							s = en.getStart().getName();
						if(en.getEnd() != null)
							e = en.getEnd().getName();
						System.out.println(s+" "+en.getDuration()+" "+e);
					}
			}
				
			
			// write one OR CLAUSE in result
			result.addSeqClause(resultSeq);
		}
		
		/*
		 *  Exp = (SEQ CLAUSE (and SEQ CLAUSE)*):= combination
		 */
		private void expand2(List<SeqClause> combination, OrClause result) throws EPLTranslateException{
			
			// time delay table entries of result
			List<TimeDelayEntry> resultTDEntries = new ArrayList<TimeDelayEntry>();
		
			// every SEQ CLAUSE in this AND CLAUSE 
			for(int i = 0; i < combination.size(); i++){
				SeqClause currentSeq = combination.get(i);
				List<Term> currentTerms = currentSeq.getTerms();
				
				// initiate time delay table of SEQ CLAUSE with only one term
				if(currentSeq.getTdTable() == null){
					initTimeDelayTable(currentSeq);
				}
				TimeDelayTable currentTDTable = currentSeq.getTdTable();
				
				
				if(currentTerms.size() < 1){
					throw new EPLTranslateException("Time delay should not be an operand of an AND operator");
				}
				else{
					for(int j = 0; j < currentTerms.size(); j++){
						if(EPLTranslateUtil.getTermType(currentTerms.get(j)) == EPLTranslateUtil.TERM_TIME){
							throw new EPLTranslateException("Time delays connected with AND could not be expanded any more");
						}
					}
					
					// make the time delay table of the result, copy every time delay table of SEQ CLAUSE
					for(TimeDelayEntry entry : currentTDTable.getEntries()){
						resultTDEntries.add(entry);
					}
				}
			}
			
			// (SEQ CLAUSE (or SEQ CLAUSE)*) = AND CLAUSE
			List<SequenceOption> seqOptions = transformAndClause(combination);
			
			
			// create result
			for(int i = 0; i < seqOptions.size(); i++){
				SeqClause resultSeq = seqOptions.get(i).getSequence();
				
				// copy time delay table for every sequence combinations
				TimeDelayTable resultTDTable = new TimeDelayTable();
				List<TimeDelayEntry> temp = resultTDTable.getEntries();
				for(int j = 0; j < resultTDEntries.size(); j++){
					TimeDelayEntry entry = resultTDEntries.get(j);
					temp.add(new TimeDelayEntry(entry.getStart(), entry.getEnd(), entry.getDuration()));
				}
				
				// fix time delay at start
				List<TimeDelayEntry> left = resultTDTable.getEntriesByStart(null);
				EPLTranslateUtil.reduceStartDelayEntry(left, resultSeq, true, resultTDTable);
				/*
				 * ([T] -> A -> *) -> * to (T -> A -> *)
				 */
				if(left.size() > 1){
					List<Term> terms = resultSeq.getTerms();
					if(left.get(0).getEnd() != terms.get(0)){
						throw new EPLTranslateException("Fixed Time delay error at start");
					}
					else{
						Term term = new Term(EPLConstants.TIMER_INTERVAL_NAME);
						term.setDuration(left.get(0).getDuration());
						terms.add(0, term);
							
						resultTDTable.getEntries().remove(left.get(0));
					}
				}
				
				// fix time delay at end
				List<TimeDelayEntry> right = resultTDTable.getEntriesByEnd(null);
				EPLTranslateUtil.reduceEndDelayEntry(right, resultSeq, true, resultTDTable);
				/*
				 * * -> (* -> A -> [T]) to * -> (* -> A -> T)
				 */
				if(right.size() > 1){
					List<Term> terms = resultSeq.getTerms();
					if(right.get(right.size()-1).getStart() != terms.get(terms.size()-1)){
						throw new EPLTranslateException("Fixed Time delay error at end");
					}
					else{
						Term term = new Term(EPLConstants.TIMER_INTERVAL_NAME);
						term.setDuration(right.get(right.size()-1).getDuration());
						terms.add(term);
							
						resultTDTable.getEntries().remove(right.get(right.size()-1));
					}
				}
				
					// for test
					System.out.println("\nAND Connected");
					List<Term> terms = resultSeq.getTerms();
					for(int j = 0; j < terms.size(); j++){
						System.out.print(terms.get(j).getName()+" + ");
					}
					System.out.println();
					
					List<TimeDelayEntry> t = resultTDTable.getEntries();
					for(int j = 0; j < t.size(); j++){
						TimeDelayEntry en = t.get(j);
						String s = "null", e = "null";
						if(en.getStart() != null)
							s = en.getStart().getName();
						if(en.getEnd() != null)
							e = en.getEnd().getName();
						System.out.println(s+" "+en.getDuration()+" "+e);
					}
					
				resultSeq.setTdTable(resultTDTable);
				result.addSeqClause(resultSeq);
			}
					
		}
		
		/*
		 * Transform AND CLAUSE into all possible sequence options
		 * 
		 * @param seqcs must not be null
		 */
		private List<SequenceOption> transformAndClause(List<SeqClause> combination) throws EPLTranslateException{
			
			List<SequenceOption> ret = new ArrayList<SequenceOption>();
			SeqClause copy = new SeqClause();
			List<Term> init = combination.get(0).getTerms();
			for(int i = 0; i < init.size(); i++){
				copy.addTerm(init.get(i));
			}
				
			ret.add(new SequenceOption(0, copy));
				
					
			// insert seq terms in AND CLAUSE
			for(int i = 1; i < combination.size(); i++){
				SeqClause iseq = combination.get(i);
						
				ret = insertSeq(ret, iseq);
			}
				
			return ret;
		}
		
		/*
		 * Create new time delay table and time delay entries for a SEQ CLAUSE containing only one term.
		 * For a time delay term, the table has only one entry "null delay null". For a event term, the 
		 * table is empty.
		 * 
		 * This method is only called by expand1 and expand2, when united AND expressions or united SEQ expressions.
		 */
		private void initTimeDelayTable(SeqClause seqc) throws EPLTranslateException{
			List<Term> terms = seqc.getTerms();
			
			if(terms.size() != 1){
				throw new EPLTranslateException("Time delay table can not be initiated");
			}
			
			Term term = terms.get(0);
			
			// create a new time delay table and set it to the SEQ CLAUSE
			TimeDelayTable tdTable = new TimeDelayTable();
			seqc.setTdTable(tdTable);
			
			// term is time delay: create one time delay entry, remove time delay term
			if(EPLTranslateUtil.getTermType(term) == EPLTranslateUtil.TERM_TIME){
				List<TimeDelayEntry> entries = tdTable.getEntries();
				entries.add(new TimeDelayEntry(null, null, term.getDuration()));
				terms.remove(0);
			}
			
			// term is event: empty table
		}
		
		/*
		 * Sequence a current SEQ CLAUSE and its time delay table to a result SEQ CLAUSE
		 *
		 * @param rt the term list of result SEQ CLAUSE
		 * @param resultTable time delay table of result SEQ CLAUSE 
		 * @param ct the term list of current SEQ CLAUSE, its content should not be changed
		 * @param currentTable time delay table of current SEQ CLAUSE
		 */
		private void sequenceSeqClauses(Term endEvent, List<Term> rt, TimeDelayTable resultTable, List<Term> ct, TimeDelayTable currentTable){
			

			Term startEvent = null; 
			if(ct.size() > 0){
				startEvent = ct.get(0);
			}
			
			List<TimeDelayEntry> end = resultTable.getEntriesByEnd(null);
			
			List<TimeDelayEntry> left = currentTable.getEntriesByStart(null);
			
				/*//for test
				if(endEvent != null){
					System.out.println("endEvent "+ endEvent.getName());
				}
				else{
					System.out.println("endEvent null");
				}
				if(startEvent != null){
					System.out.println("startEvent "+ startEvent.getName());
				}
				else{
					System.out.println("startEvent null");
				}
				System.out.println("endSize "+end.size());
				System.out.println("leftSize "+left.size());*/
			
			/*
			 * null
			 * * -> A
			 * * -> A -> T
			 * * -> T -> T
			 */
			if(end.size() == 0){
				/*
				 * * -> A
				 * * -> A -> T 
				 * * -> T -> T
				 */
				if(endEvent != null){
					/*
					 * * -> A -> T
					 * * -> T -> T
					 */
					if(EPLTranslateUtil.getTermType(endEvent) == EPLTranslateUtil.TERM_TIME){
						/*
						 * A -> *
						 * T -> A -> *
						 * T -> T -> *
						 */
						if(left.size() == 0){
							
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
							
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
							
							
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
						}
						/*
						 * [T] -> *
						 * (T -> A -> *) -> *
						 * 
						 */
						else if(left.size() == 1){
							
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
							
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
							
							
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
						}
						/*
						 * \\([T] -> A -> *) -> *
						 * ((T -> A -> *) -> B -> *) -> *
						 */
						else{
							
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
							
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
							
							
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
						}
					}
					/*
					 * * -> A
					 */
					else{
						/*
						 * A -> *
						 * T -> A -> *
						 * T -> T -> *
						 */
						if(left.size() == 0){
							
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
							
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
							
							
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
						}
						/*
						 * [T] -> *
						 * (T -> A -> *) -> *
						 * 
						 */
						else if(left.size() == 1){
							
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
							
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
							
							
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
						}
						/*
						 * \\([T] -> A -> *) -> *
						 * ((T -> A -> *) -> B -> *) -> *
						 */
						else{
							
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
							
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
							
							
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
						}
					}
				}
				// the first SEQ CLAUSE
				/*
				 * null
				 */
				else{
					
					for(int j = 0; j < ct.size(); j++){
						rt.add(ct.get(j));
					}
					
					for(int j = 0; j < end.size(); j++){
						end.get(j).setEnd(startEvent);
					}
					
					for(int j = 0; j < left.size(); j++){
						left.get(j).setStart(endEvent);
					}
					
					
					List<TimeDelayEntry> entries = resultTable.getEntries();
					for(TimeDelayEntry entry : currentTable.getEntries()){
						entries.add(entry);
					}
				}
			}
			/*
			 * * -> [T] 
			 * * -> (* -> A -> T)  
			 */
			else if(end.size() == 1){
				/*
				 * null -> [T]
				 */
				if(endEvent == null){
					
					/*
					 * [T] -> *
					 * (T -> A -> *) -> *
					 */
					if(left.size() == 1){
						/*
						 * [T] -> null
						 */
						if(startEvent == null){
							
							end.get(0).setDuration(end.get(0).getDuration()+left.get(0).getDuration());
							currentTable.getEntries().remove(left.get(0));
							
						}
						/*
						 * [T] -> event*
						 * (T -> A -> *) -> *
						 */
						else{
						
							if(EPLTranslateUtil.getTermType(startEvent) == EPLTranslateUtil.TERM_EVENT){
								end.get(0).setDuration(end.get(0).getDuration()+left.get(0).getDuration());
								currentTable.getEntries().remove(left.get(0));
							}
							
						}
					}
					
					// common part
					/*
					 * A -> *
					 * T -> A -> * 
					 * T -> T -> *
					 * 
					 * [T] -> *
					 * (T -> A -> *) -> * 
					 * 
					 * \\([T] -> A -> *) -> *
					 * ((T -> A -> *) -> B -> *) -> *
					 */
					for(int j = 0; j < ct.size(); j++){
						rt.add(ct.get(j));
					}
						
					for(int j = 0; j < end.size(); j++){
						end.get(j).setEnd(startEvent);
					}
					
					for(int j = 0; j < left.size(); j++){
						left.get(j).setStart(endEvent);
					}
						
						
					List<TimeDelayEntry> entries = resultTable.getEntries();
					for(TimeDelayEntry entry : currentTable.getEntries()){
						entries.add(entry);
					}
				}
				/*
				 * event* -> [T] 
				 * * -> (* -> A -> T)  
				 */
				else{
					/*
					 * * -> (* -> A -> T)
					 */
					if(EPLTranslateUtil.getTermType(endEvent) == EPLTranslateUtil.TERM_TIME){
							
						for(int j = 0; j < ct.size(); j++){
							rt.add(ct.get(j));
						}
							
						for(int j = 0; j < end.size(); j++){
							end.get(j).setEnd(startEvent);
						}
							
						for(int j = 0; j < left.size(); j++){
							left.get(j).setStart(endEvent);
						}
							
							
						List<TimeDelayEntry> entries = resultTable.getEntries();
						for(TimeDelayEntry entry : currentTable.getEntries()){
							entries.add(entry);
						}
					
					}
					/*
					 * event* -> [T]
					 */
					else{
						/*
						 * A -> *
						 * T -> A -> *
						 * T -> T -> *
						 */
						if(left.size() == 0){
							
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
								
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
								
								
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
							
						}
						/*
						 * [T] -> *
						 * (T -> A -> *) -> *
						 */
						else if(left.size() == 1){
							
							/*
							 * [T] -> null
							 */
							if(startEvent == null){
								
								end.get(0).setDuration(end.get(0).getDuration()+left.get(0).getDuration());
								currentTable.getEntries().remove(left.get(0));
								
								for(int j = 0; j < ct.size(); j++){
									rt.add(ct.get(j));
								}
									
								for(int j = 0; j < end.size(); j++){
									end.get(j).setEnd(startEvent);
								}
								
								for(int j = 0; j < left.size(); j++){
									left.get(j).setStart(endEvent);
								}
									
									
								List<TimeDelayEntry> entries = resultTable.getEntries();
								for(TimeDelayEntry entry : currentTable.getEntries()){
									entries.add(entry);
								}
							}
							/*
							 * [T] -> event*
							 * (T -> A -> *) -> *
							 */
							else{
							
								if(EPLTranslateUtil.getTermType(startEvent) == EPLTranslateUtil.TERM_EVENT){
									
									end.get(0).setDuration(end.get(0).getDuration()+left.get(0).getDuration());
									currentTable.getEntries().remove(left.get(0));
									
								}
								
								for(int j = 0; j < ct.size(); j++){
									rt.add(ct.get(j));
								}
									
								for(int j = 0; j < end.size(); j++){
									end.get(j).setEnd(startEvent);
								}
								
								for(int j = 0; j < left.size(); j++){
									left.get(j).setStart(endEvent);
								}
									
									
								List<TimeDelayEntry> entries = resultTable.getEntries();
								for(TimeDelayEntry entry : currentTable.getEntries()){
									entries.add(entry);
								}
							}
						}
						/*
						 * \\([T] -> A -> *) -> *
						 * ((T -> A -> *) -> B -> *) -> *
						 */
						else{
							for(int j = 0; j < ct.size(); j++){
								rt.add(ct.get(j));
							}
								
							for(int j = 0; j < end.size(); j++){
								end.get(j).setEnd(startEvent);
							}
							
							for(int j = 0; j < left.size(); j++){
								left.get(j).setStart(endEvent);
							}
								
								
							List<TimeDelayEntry> entries = resultTable.getEntries();
							for(TimeDelayEntry entry : currentTable.getEntries()){
								entries.add(entry);
							}
						}
					}
				}
			}
			/*
			 * \\* -> (* -> A -> [T])  
			 * * -> (* -> B -> ( * -> A -> T))
			 */
			else{
				
				/*
				 * * -> *
				 */
				for(int j = 0; j < ct.size(); j++){
					rt.add(ct.get(j));
				}
					
				for(int j = 0; j < end.size(); j++){
					end.get(j).setEnd(startEvent);
				}
				
				for(int j = 0; j < left.size(); j++){
					left.get(j).setStart(endEvent);
				}
					
					
				List<TimeDelayEntry> entries = resultTable.getEntries();
				for(TimeDelayEntry entry : currentTable.getEntries()){
					entries.add(entry);
				}
			}
		}

		
		
		
		/*
		 * Insert the terms in iseq into the seq. For every inserted seq create a new SEQ CLAUSE.
		 * 
		 * @param seq the original sequence into which iseq is inserted
		 * @param iseq the sequence to be inserted
		 */
		private List<SequenceOption> insertSeq(List<SequenceOption> seqs, SeqClause iseq) throws EPLTranslateException{
			List<SequenceOption> ret = new ArrayList<SequenceOption>();
			
			List<SequenceOption> temp = new ArrayList<SequenceOption>();
			List<Term> iterms = iseq.getTerms();
			
			if(seqs.size() > 0){
				// every sequence into which the iseq should be inserted
				for(int i = 0; i < seqs.size(); i++){
					SequenceOption seq = seqs.get(i);
					// default start position for a iseq is 0
					seq.setInsertIndex(0);
					temp.add(seq);
					
					// every term in iseq is inserted into sequence
					for(int j = 0; j < iterms.size(); j++){
						Term iterm = iterms.get(j);
						temp = insertTerm(temp, iterm);
					}
					
					// add new sequences in result
					for(int j = 0; j < temp.size(); j++){
						
						if(ret.size() > MAX_NUM_SEQ_CLAUSE){
							throw new EPLTranslateException("The number of SEQ Clause is larger than "+MAX_NUM_SEQ_CLAUSE);
						}
						
						ret.add(temp.get(j));
					}
					temp.clear();
				}
			}
			// when seqs is empty sequences, copy iseq as sequence
			else{
				ret.add(new SequenceOption(0, iseq));
			}
			
			return ret;
		}
		
		/*
		 * Insert the term into the seq starting from the index. For every insertion create a new SEQ CLAUSE
		 * 
		 * @param term the term should be inserted into sequence
		 * @param seqs all sequences into which the term should be inserted 
		 * 
		 * @return new sequences with inserted term
		 * 
		 */
		private List<SequenceOption> insertTerm(List<SequenceOption> seqs, Term term){
			
			List<SequenceOption> ret = new ArrayList<SequenceOption>();
			
			// every sequence into which the term should be inserted
			for(int i = 0; i < seqs.size(); i++){
				SequenceOption seq = seqs.get(i);
				int index = seq.getInsertIndex();
				SeqClause ostrs = seq.getSequence();
				List<Term> otrs = ostrs.getTerms();
				
					/*//for test
					System.out.print("org seq: ");
					for(int j = 0; j < otrs.size(); j++){
						System.out.print(otrs.get(j).getType()+" ");
					}
					System.out.println();*/
				
				// insert the term into sequence starting from index
				for(int j = index; j < otrs.size(); j++){
					SeqClause strs = new SeqClause();
					List<Term> ltr = strs.getTerms();
					
					for(int k = 0; k < otrs.size(); k++){
						if(k == j){
							ltr.add(term);
						}
						ltr.add(otrs.get(k));
					}
					
					// add new sequence after insertion
					SequenceOption instrs = new SequenceOption(j+1, strs);
					ret.add(instrs);
						
						//System.out.println("add term "+term.getType()+" at "+j);
				}
						
						//System.out.println("add term "+term.getType()+" at "+otrs.size());
				// insert the term into the sequence at the end of sequence
				otrs.add(otrs.size(), term);
				// add new sequence after insertion
				seq.setInsertIndex(otrs.size());
				ret.add(seq);
					
			}
			
			return ret;
		}
		
		/*
		 * Get EPL from standard expression.
		 */
		private String getEPL(OrClause result, EPLTranslatorData data) throws EPLTranslateException{
			StringBuffer epl = new StringBuffer();
			
			epl.append(String.format(EPLConstants.SELECT, "*")+" ");
			epl.append(String.format(EPLConstants.FROM_PATTERN, "", getPatternExpression(result, data))+" ");
			
			long wDuration = data.getWindowDuration();
			if(wDuration > 0){
				String wType = data.getWindowType();
				if(wType.equalsIgnoreCase("sliding")){
					epl.append(String.format(EPLConstants.WINDOW_SLIDING, wDuration));
				}
				else if(wType.equalsIgnoreCase("tumbling")){
					epl.append(String.format(EPLConstants.WINDOW_TUMBLING, wDuration));
				}
				else{
					throw new EPLTranslateException("Unsupported window type "+wType);
				}
			}
			
			return epl.toString();
		}
		
		/*
		 * Get EPL of event pattern part. 
		 */
		private String getPatternExpression(OrClause result, EPLTranslatorData data) throws EPLTranslateException{
			StringBuffer ret = new StringBuffer();
			
			NotTable notTable = data.getNotTable();
			
			String prologText = data.getPrologText().toString();
			
			List<SeqClause> seqcs = result.getSeqClauses();
			
			if(seqcs.size() > 0){
					
					//for test
					System.out.println("\nTotal SEQ CLAUSE size: "+seqcs.size());
				
					for(int i = 0; i < seqcs.size(); i++){
						SeqClause seq = seqcs.get(i);
						List<Term> ts = seq.getTerms();
						System.out.println("\n"+i);
						for(int j = 0; j < ts.size(); j++){
							System.out.print(ts.get(j).getName()+" + ");
						}
						System.out.println("\nTimeDelayTable: ");
						TimeDelayTable tdTable = seq.getTdTable();
						if(tdTable != null){
							List<TimeDelayEntry> entries = tdTable.getEntries();
							
							for(int j = 0; j < entries.size(); j++){
								TimeDelayEntry en = entries.get(j);
								String s = "null", e = "null";
								if(en.getStart() != null)
									s = en.getStart().getName();
								if(en.getEnd() != null)
									e = en.getEnd().getName();
								System.out.println(s+" "+en.getDuration()+" "+e);
							}
						}
					}
					System.out.println();
				
				/*
				 * eIndex[i]: the last event tag number of the ith SEQ CLAUSE
				 * nIndex[i]: the last not event tag number of the ith SEQ CLASE
				 */
				int eIndexs [] = new int [seqcs.size()];
				int nIndexs [] = new int [seqcs.size()];
				
				ret.append(getSeqClauseExpression(seqcs.get(0), notTable, 0, eIndexs, nIndexs, prologText));
			
				for(int i = 1; i < seqcs.size(); i++){
					ret.append(EPLConstants.OPERATOR_OR+" ");
					ret.append(getSeqClauseExpression(seqcs.get(i), notTable, i, eIndexs, nIndexs, prologText));
				}
			}
			
			return ret.toString();
		}
		
		/*
		 * Get EPL of SEQ CLAUSE part.
		 * 
		 * @param sIndex: the index of this SEQ clause in OR clause
		 * @param eIndexs: the last index of event tag for each SEQ clause in OR clause
		 * @param nIndexs: the last index of not event tag for each SEQ clause in OR clause
		 */
		private String getSeqClauseExpression(SeqClause seqc, NotTable notTable, int sIndex, int [] eIndexs, int [] nIndexs, String prologText) throws EPLTranslateException{
			StringBuffer ret = new StringBuffer();
			List<Term> ltrs = seqc.getTerms();
			Map<Term, Term> notEventList = new HashMap<Term, Term>();
			TimeDelayTable tdTable = seqc.getTdTable();
			Stack<IEntry> openParaStack = new Stack<IEntry>();
			IEntry stackTop = null;
			StringBuffer sparqlText = new StringBuffer();
			
			List<String> eventVarNames = new ArrayList<String>();
			
			/*
			 * eStart: start number of event tag in this SEQ CLAUSE
			 * eIndex: the largest event tag number
			 * nStart: start number of not event tag in this SEQ CLAUSE
			 * nIndex: the largest not event tag number
			 */
			int eStart = 1, eIndex = 1, nStart = 1, nIndex = 1;
			
			int i1 = sIndex-1;
			while(i1 > -1){
				if(eIndexs[i1] != 0){
					eStart = eIndexs[i1]+1;
					eIndex = eStart;
					break;
				}
				i1--;
			}
			
			i1 = sIndex-1;
			while(i1 > -1){
				if(nIndexs[i1] != 0){
					nStart = nIndexs[i1]+1;
					nIndex = nStart;
					break;
				}
				i1--;
			}
			
			ret.append("( ");
			
			if(ltrs.size() > 0){
				StringBuffer eParams = new StringBuffer();
				
				// SEQ CLAUSE never be united by AND or SEQ
				if(tdTable == null){
					if(ltrs.size() > 1){
						throw new EPLTranslateException("One sequence clause has no time delay table");
					}
					else{
						Term term = ltrs.get(0);
						
						ret.append(EPLConstants.EVERY+" ");
						
						if(EPLTranslateUtil.getTermType(term) == EPLTranslateUtil.TERM_TIME){
							
							ret.append(String.format(EPLConstants.TIMER_INTERVAL, term.getDuration())+" ");
						}
						else{
							
							ret.append(EPLConstants.EVENTTAG+eIndex+"=");
							eIndexs[sIndex] = eIndex;
							eIndex++;
							
							sparqlText.append(term.getSparqlText());
							
							eventVarNames.add(term.getVarName());
							for(int m = 1; m < eventVarNames.size(); m++){
								for(int n = 0; n < m; n++){
									sparqlText.append(" "+String.format(BDPLConstants.SPARQL_FILTER_VAR_NOT_EQUAL, eventVarNames.get(n), eventVarNames.get(m)));
								}
							}
							
							eParams.append("{");
							int k = eStart;
							for(; k < eIndex-1; k++){
								eParams.append(EPLConstants.EVENTTAG+k+",");
							}
							eParams.append(EPLConstants.EVENTTAG+k+"}");
							
							//System.out.print(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, prologText+String.format(EPLConstants.SPARQL_ASK_QUERY, sparqlText), eParams.toString())+") ");
							//ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, prologText+String.format(BDPLConstants.SPARQL_ASK_QUERY, sparqlText), eParams.toString())+") ");
							//matchedPatternSparql.add(prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText));
							ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RESULT_BINDING, prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText), eParams.toString())+") ");
							injectParams.add(EPLTranslationData.INJECT_PARA_REALTIMERESULT_BINDING_DATA);
							
							eParams.delete(0, eParams.length());
						}
					}
				}
				
				/*
				 *              A         A  
				 *         T -> T         T -> T                        
				 *         A -> T         T -> A 
				 *            [T]    ->   [T]
				 *        A -> T)         (T -> A  
				 * B -> (A -> T))         ((T -> A) -> B  
				 *  
				 */
				else{
					/*
					 *  Time delay entries staring from null
					 *  
					 *  (...
					 */
					List<TimeDelayEntry> left = tdTable.getEntriesByStart(null);
					EPLTranslateUtil.sortTimeDelayEntryByEnd(left, seqc);
					
					for(int i = left.size()-1; i > -1 ; i--){
						ret.append("( ");
						openParaStack.push(left.get(i));
						stackTop = openParaStack.peek();
					}
					
					
					Term term = ltrs.get(0);
					
					/*
					 * Time delay entries ending at the 1st term
					 * 
					 *  (...) 
					 */
					List<TimeDelayEntry> right = tdTable.getEntriesByEnd(term);
					EPLTranslateUtil.sortTimeDelayEntryByStart(right, seqc);
					
					String firstEvery = EPLConstants.EVERY;
					if(right.size() <= 1){
						if(right.size() == 1){
							if(right.get(0) == stackTop){
								
								ret.append(firstEvery+" "+String.format(EPLConstants.TIMER_INTERVAL, right.get(0).getDuration())+") "+EPLConstants.OPERATOR_SEQ+" ");
								
								openParaStack.pop();
								if(openParaStack.size() > 0){
									stackTop = openParaStack.peek();
								}
								else{
									stackTop = null;
								}
								
								firstEvery = "";
							}
							else{
								throw new EPLTranslateException("Time delays are overlapping");
							}
						}
					}
					else{
						throw new EPLTranslateException("Time delays are duplicated at begin");
					}
					
					
					/*
					 * The 1st term
					 * 
					 * (...) -> term
					 */
					boolean lastTermTime = false;
					if(EPLTranslateUtil.getTermType(term) == EPLTranslateUtil.TERM_TIME){
							//System.out.print(EPLConstants.TIMER_INTERVAL+"("+term.getDuration()+") ");
						ret.append(firstEvery+" "+String.format(EPLConstants.TIMER_INTERVAL, term.getDuration())+" ");
						lastTermTime = true;
					}
					else{
							//System.out.print(EPLConstants.EVENTTAG+eIndex+"=");
						ret.append(firstEvery+" "+EPLConstants.EVENTTAG+eIndex+"=");
						eIndexs[sIndex] = eIndex;
						eIndex++;
						
						sparqlText.append(term.getSparqlText());
						
						eventVarNames.add(term.getVarName());
						for(int m = 1; m < eventVarNames.size(); m++){
							for(int n = 0; n < m; n++){
								sparqlText.append(" "+String.format(BDPLConstants.SPARQL_FILTER_VAR_NOT_EQUAL, eventVarNames.get(n), eventVarNames.get(m)));
							}
						}
						
						eParams.append("{");
						int k = eStart;
						for(; k < eIndex-1; k++){
							eParams.append(EPLConstants.EVENTTAG+k+",");
						}
						eParams.append(EPLConstants.EVENTTAG+k+"}");
						
						
						
						if(ltrs.size() > 1){
							ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText), eParams.toString())+") ");
						}
						else{
							//ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, prologText+String.format(BDPLConstants.SPARQL_ASK_QUERY, sparqlText), eParams.toString())+") ");
							//matchedPatternSparql.add(prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText));
							ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RESULT_BINDING, prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText), eParams.toString())+") ");
							injectParams.add(EPLTranslationData.INJECT_PARA_REALTIMERESULT_BINDING_DATA);
						}
						
						eParams.delete(0, eParams.length());
					}
					
					
					// register not list
					boolean notOpenPara = false;
					NotEntry entry = notTable.getEntryByNotStart(term);
					if(entry != null){
						if(EPLTranslateUtil.getTermType(entry.getNot()) == EPLTranslateUtil.TERM_TIME){
							openParaStack.push(entry);
							stackTop = openParaStack.peek();
							notOpenPara = true;
						}
						else{
							// put only not event in list
							notEventList.put(entry.getNotEnd(), entry.getNot());
						}
					}
					
					
					
					boolean thisTermTime = false;
					for(int i = 1; i < ltrs.size(); i++){
						// last term
						Term lastTerm = term;
						// term
						term = ltrs.get(i);
						
						
						/*
						 * Time delay entries starting from last term
						 *  
						 * last term -> (...
						 */
						left = tdTable.getEntriesByStart(lastTerm);
						EPLTranslateUtil.sortTimeDelayEntryByEnd(left, seqc);
							
							/*
							// for test
							for(int j = left.size()-1; j >-1; j--){
								TimeDelayEntry temp = left.get(j);
								String s = "null", e = "null";
								if(temp.getStart() != null){
									s = temp.getStart().getType();
								}
								if(temp.getEnd() != null){
									e = temp.getEnd().getType();
								}
								System.out.println("Left Entry: "+s+" "+temp.getDuration()+" "+e);
							}*/
						
						
						/*
						 * Time delay entries ending at this term
						 *  
						 * last term ... ) -> term
						 */
						right = tdTable.getEntriesByEnd(term);
						EPLTranslateUtil.sortTimeDelayEntryByStart(right, seqc);
							
							/*for(int j = 0; j < right.size(); j++){
								System.out.println(term.getName()+" end "+right.get(j).getStart().getName());
							}*/
						
						/*
						 * Time delay after last term
						 * 
						 * last term -> time:delay
						 */
						String interval = null;
						if(left.size() > 0){
							if(right.size() > 0 && right.get(right.size()-1) == left.get(0)){
								lastTermTime = true;
								
								interval = EPLConstants.OPERATOR_SEQ+" "+String.format(EPLConstants.TIMER_INTERVAL, left.get(0).getDuration())+" ";
								
								for(Term not : notEventList.values()){
									interval += EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+EPLConstants.NOTEVENTTAG+nIndex+"="+not.getName();
									
									// add triple end '.'
									String temp = sparqlText.toString().trim();
									if(temp.length() > 0 && temp.charAt(temp.length()-1) != '.'){
										sparqlText.append(EPLConstants.TRIPLEEND+" ");
									}
									
									int se = sparqlText.length();
									sparqlText.append(not.getSparqlText());
									
									
									for(int m = 1; m < eventVarNames.size(); m++){
										for(int n = 0; n < m; n++){
											sparqlText.append(" "+String.format(BDPLConstants.SPARQL_FILTER_VAR_NOT_EQUAL, eventVarNames.get(n), eventVarNames.get(m)));
										}
									}
									for(String evn : eventVarNames){
										sparqlText.append(" "+String.format(BDPLConstants.SPARQL_FILTER_VAR_NOT_EQUAL, evn, not.getVarName()));
									}
									
									
									eParams.append("{");
									int k = eStart;
										
									for(; k < eIndex; k++){
										eParams.append(EPLConstants.EVENTTAG+k+",");
									}
									
									
									
									interval += ("("+String.format(EPLConstants.FILTER_RDF, prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText), eParams.toString()+EPLConstants.NOTEVENTTAG+nIndex+"}")+") ");
									sparqlText.delete(se, sparqlText.length());
									
									nIndexs[sIndex] = nIndex;
									nIndex++;
									
									eParams.delete(0, eParams.length());
									
								}
								
								right.remove(right.size()-1);
								left.remove(0);
							}
						}
						
						/*
						 *  Close parenthesis after last term
						 * 
						 *  last term ... ) and time:interval
						 *  
						 */
						boolean flag1 = false;
						for(int j = 0; j < right.size(); j++){
							TimeDelayEntry temp = right.get(j);
							if(temp == stackTop){
								if(notOpenPara){
									throw new EPLTranslateException("Not time delay can not start from a time delay");
								}
								
								flag1 = true;
								
								ret.append(") "+EPLConstants.OPERATOR_AND+" "+String.format(EPLConstants.TIMER_INTERVAL, temp.getDuration())+" ");
	
								openParaStack.pop();
								if(openParaStack.size() > 0){
									stackTop = openParaStack.peek();
								}
								else{
									stackTop = null;
								}
							}
							else{
								throw new EPLTranslateException("Time delays are overlapping");
							}
						}
						
						/* 
						 * Time Delay between last and this term
						 * 
						 * last term and not ... ) and time:interval -> time:interval and not ...
						 */
						if(interval != null){
							//System.out.print(interval);
							ret.append(interval);
						}
						
						//System.out.print(EPLConstants.OPERATOR_SEQ+" ");
						ret.append(EPLConstants.OPERATOR_SEQ+" ");
						
						/*
						 * Open paras on the right of this term ( Time delay starting from last term )
						 * 
						 * last term and not ... ) and time:interval -> ... -> (
						 * 
						 */
						boolean flag2 = false;
						for(int j = left.size()-1; j > -1 ; j--){
							flag2 = true;
							//System.out.print("( ");
							ret.append("( ");
							openParaStack.push(left.get(j));
							stackTop = openParaStack.peek();
						}
						
						/*
						 * Open paras on the right of this term ( Not time starting from this term)
						 * 
						 * last term and not ... ) and time:interval -> ... -> (
						 * 
						 */
						if(notOpenPara){
							//System.out.print("( ");
							ret.append("( ");
						}
						
						/*
						 * This Term
						 * 
						 * last term and not ... ) and time:interval -> ... -> ( this term
						 * 
						 */
						StringBuffer sparqlTextNOT = new StringBuffer(sparqlText);
						if(EPLTranslateUtil.getTermType(term) == EPLTranslateUtil.TERM_TIME){
							
							ret.append(String.format(EPLConstants.TIMER_INTERVAL, term.getDuration())+" "); 
							thisTermTime = true;
						}
						else{
							
							ret.append(EPLConstants.EVENTTAG+eIndex+"=");
							eIndexs[sIndex] = eIndex;
							eIndex++;
							
							// add triple end '.'
							String temp = sparqlText.toString().trim();
							
							if(temp.length() > 0 && temp.charAt(temp.length()-1) != '.'){
								sparqlText.append(EPLConstants.TRIPLEEND+" ");
							}
							
							sparqlText.append(term.getSparqlText());
							
							eventVarNames.add(term.getVarName());
							for(int m = 1; m < eventVarNames.size(); m++){
								for(int n = 0; n < m; n++){
									sparqlText.append(" "+String.format(BDPLConstants.SPARQL_FILTER_VAR_NOT_EQUAL, eventVarNames.get(n), eventVarNames.get(m)));
								}
							}
							
							eParams.append("{");
							int k = eStart;
							for(; k < eIndex-1; k++){
								eParams.append(EPLConstants.EVENTTAG+k+",");
							}
							eParams.append(EPLConstants.EVENTTAG+k+"}");
							
							
							if(i < ltrs.size()-1){
								ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText), eParams.toString())+") ");
							}
							else{
								//ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, prologText+String.format(BDPLConstants.SPARQL_ASK_QUERY, sparqlText), eParams.toString())+") ");
								//matchedPatternSparql.add(prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText));
								ret.append(term.getName()+"("+String.format(EPLConstants.FILTER_RESULT_BINDING, prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlText), eParams.toString())+") ");
								injectParams.add(EPLTranslationData.INJECT_PARA_REALTIMERESULT_BINDING_DATA);
							}
							eParams.delete(0, eParams.length());
						}
						
						//XXX check
						if(!lastTermTime && !thisTermTime){
							if(flag1 && flag2){
								throw new EPLTranslateException("Time delays are overlapping");
							}
						}
						lastTermTime = thisTermTime;
						
						
						/*
						 * Not Event of this term
						 * 
						 * last term and not ... ) and time:interval -> ... -> ( this term and not ...
						 * 
						 */
						for(Term not : notEventList.values()){
							//System.out.print(EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+EPLConstants.NOTEVENTTAG+nIndex+"="+not.getName());
							ret.append(EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+EPLConstants.NOTEVENTTAG+nIndex+"="+not.getName());
							
							// add triple end '.'
							String temp = sparqlTextNOT.toString().trim();
							if(temp.length() > 0 && temp.charAt(temp.length()-1) != '.'){
								sparqlTextNOT.append(EPLConstants.TRIPLEEND+" ");
							}
							
							int se = sparqlTextNOT.length();
							sparqlTextNOT.append(not.getSparqlText());
							
							
							for(int m = 1; m < eventVarNames.size(); m++){
								for(int n = 0; n < m; n++){
									sparqlTextNOT.append(" "+String.format(BDPLConstants.SPARQL_FILTER_VAR_NOT_EQUAL, eventVarNames.get(n), eventVarNames.get(m)));
								}
							}
							for(String evn : eventVarNames){
								sparqlTextNOT.append(" "+String.format(BDPLConstants.SPARQL_FILTER_VAR_NOT_EQUAL, evn, not.getVarName()));
							}
							
							eParams.append("{");
							int k =eStart;
							if(thisTermTime){
								for(; k < eIndex; k++){
									eParams.append(EPLConstants.EVENTTAG+k+",");
								}
							}
							else{
								for(; k < eIndex-1; k++){
									eParams.append(EPLConstants.EVENTTAG+k+",");
								}
							}
							
				
							ret.append("("+String.format(EPLConstants.FILTER_RDF, prologText+" %s "+String.format(BDPLConstants.SPARQL_WHERE_CLAUSE, sparqlTextNOT), eParams.toString()+EPLConstants.NOTEVENTTAG+nIndex+"}")+") ");
							sparqlTextNOT.delete(se, sparqlTextNOT.length());
							
							nIndexs[sIndex] = nIndex;
							nIndex++;
							
							eParams.delete(0, eParams.length());
						}
						
						if(notEventList.get(term) != null){
							// not list should be empty, after processing every term!
							notEventList.remove(term);
						}
						
						/*
						 * Not Time of this event
						 */
						NotEntry notTime = notTable.getEntryByNotEnd(term);
						if(notTime != null){
							if(EPLTranslateUtil.getTermType(notTime.getNot()) == EPLTranslateUtil.TERM_TIME){
								if(notTime == stackTop){
									//System.out.print(") "+EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+EPLConstants.TIMER_INTERVAL+"("+notTime.getNot().getDuration()+") ");
									ret.append(") "+EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+String.format(EPLConstants.TIMER_INTERVAL, notTime.getNot().getDuration())+" ");
									openParaStack.pop();
									if(openParaStack.size() > 0){
										stackTop = openParaStack.peek();
									}
									else{
										stackTop = null;
									}
								}
								else{
									throw new EPLTranslateException("Time delays are overlapping");
								}
							}
						}
						
						notOpenPara = false;
						entry = notTable.getEntryByNotStart(term);
						if(entry != null){
							if(EPLTranslateUtil.getTermType(entry.getNot()) == EPLTranslateUtil.TERM_TIME){
								openParaStack.push(entry);
								stackTop = openParaStack.peek();
								notOpenPara = true;
							}
							else{
								// put only not event in list
								notEventList.put(entry.getNotEnd(), entry.getNot());
							}
						}
					}
					
					
					// term -> ) : time delay entries ending at end
					right = tdTable.getEntriesByEnd(null);
					EPLTranslateUtil.sortTimeDelayEntryByStart(right, seqc);
						/*for(int i = 0; i < right.size(); i++){
							System.out.println("null end "+right.get(i).getStart().getName());
						}*/
					
					// term -> ( : time delay entries starting from the last term
					left = tdTable.getEntriesByStart(term);
					EPLTranslateUtil.sortTimeDelayEntryByEnd(left, seqc);
						
						//System.out.println(term.getName()+" start "+left.size());
					if(left.size() <= 1){
						if(left.size() == 1){
							if(right.size() > 0 && right.get(right.size()-1) == left.get(0)){
								//System.out.print(EPLConstants.OPERATOR_SEQ+" ( "+EPLConstants.TIMER_INTERVAL+"("+left.get(0).getDuration()+") ");
								ret.append(EPLConstants.OPERATOR_SEQ+" ( "+String.format(EPLConstants.TIMER_INTERVAL, left.get(0).getDuration())+" ");
								right.remove(right.size()-1);
							}
						}
					}
					else{
						throw new EPLTranslateException("Time delays are duplicated at end");
					}
					
					
					for(int i = 0; i < right.size(); i++){
						TimeDelayEntry temp = right.get(i);
						if(temp == stackTop){
							//System.out.print(") "+EPLConstants.OPERATOR_AND+" "+EPLConstants.TIMER_INTERVAL+"("+temp.getDuration()+") ");	
							ret.append(") "+EPLConstants.OPERATOR_AND+" "+String.format(EPLConstants.TIMER_INTERVAL, temp.getDuration())+" ");
							openParaStack.pop();
							if(openParaStack.size() > 0){
								stackTop = openParaStack.peek();
							}
							else{
								stackTop = null;
							}
						}
						else{
							throw new EPLTranslateException("Time delays are overlapping");
						}
					}
				}
			}
			// only one connected time delay, must have time delay table
			else{
				if(tdTable == null){
					throw new EPLTranslateException("Null seq clause expession");
				}
				else{
					TimeDelayEntry temp = tdTable.getEntries().get(0);
					//System.out.print(EPLConstants.TIMER_INTERVAL+"("+temp.getDuration()+") ");
					ret.append(EPLConstants.EVERY+" "+String.format(EPLConstants.TIMER_INTERVAL, temp.getDuration())+" ");
				}
			}
			
			ret.append(") ");
			return ret.toString();
		}
		
		
		/*
		 * (AND CLAUSE (op AND CLAUSE)*):=combi (or (AND CLAUSE (op AND CLAUSE)*))* = ( AND CLAUSE (or AND CLAUSE)*):=exp (op (AND CLAUSE (or AND CLAUSE)*))*
		 * 
		 * 
		 * Find every combination composed of one SeqClause from each expression by recursive method calling.
		 * 
		 * @param combi a combination of and terms from each exps
		 * @param exps 
		 
		private void combineSeqClauseRecursive(List<SeqClause> combination, List<OrClause> exps, int depth, int type, OrClause result) throws EPLTranslateException{
			
			if(exps.size() > 0){
				// make combination of and terms from every expansion
				if(depth < exps.size()){
					OrClause otrs = exps.get(depth);
					
					for(int i = 0; i < otrs.getSeqClauses().size(); i++){
						SeqClause seqc = otrs.getSeqClauses().get(i);
						// add the next and terms in this level
						combination.add(seqc);
						// go to the next depth
						combineSeqClauseRecursive(combination, exps, depth+1, type, result);
						// remove the old and terms in this level
						combination.remove(combination.size()-1);
					}
				}
				else{
					switch(type){
						case 1:{
							// (SEQ CLAUSE (seq SEQ CLAUSE)*) (or (SEQ CLAUSE (seq SEQ CLAUSE)*)) = (SEQ CLAUSE (or SEQ CLAUSE)*)(seq (SEQ CLAUSE (or SEQ CLAUSE)*))*
							expand1(combination, result);
							break;
						}
						case 2:{
							// AND CLAUSE (or AND CLAUSE)* = (AND CLAUSE (or AND CLAUSE)*)(and (AND CLAUSE (or AND CLAUSE)*))*
							expand2(combination, result);
							break;
						}
					}
				}
			}
		}*/
		

		
		/*
		 * Make a time delay table for a SEQ CLAUSE
		 
		private void makeTimeDelayTable(SeqClause seqc){
			// create a new time delay table and set it to the SEQ CLAUSE
			TimeDelayTable tdTable = new TimeDelayTable();
			seqc.setTdTable(tdTable);
			
			List<TimeDelayEntry> entries = tdTable.getEntries();
			List<Term> terms = seqc.getTerms();
			
			Term pre = null;
			long delay = 0;
			List<Integer> indexDelay = new ArrayList<Integer>();
			int state = 0;
			// every term in the SEQ CLAUSE
			for(int i = 0; i < terms.size(); i++){
				Term term = terms.get(i);
				
				if(BDPLTransformerUtil.getTermType(term) == BDPLTransformerUtil.TERM_TIME){
					indexDelay.add(i);
					delay += term.getDuration();
					
					state = 1;
				}
				else if(BDPLTransformerUtil.getTermType(term) == BDPLTransformerUtil.TERM_EVENT){
					switch(state){
					// previous term is null
					case 0:{
						
						// set pre to this event term
						pre = term;
							
						state = 2;
						
						break;
					}
					// previous term is a time delay
					case 1:{
						System.out.println("go");
						// create a time delay entry, reset pre to this event term
						
						TimeDelayEntry entry = new TimeDelayEntry(pre, term, delay);
						entries.add(entry);
							
								if(pre != null){
									System.out.println("Time delay entry: "+pre.getName()+" "+term.getName()+" "+delay);
								}
								else{
									System.out.println("Time delay entry: null "+term.getName()+" "+delay);
								}
								
						delay = 0;
						pre = term;
							
						state = 2;
						
						break;
					}
					// previous term is an event
					case 2:{
						
						// reset pre to this event term
						pre = term;
							
						state = 2;
						
						break;
					}
					}
				}
				
				switch(state){
					// previous term is null
					case 0:{
						// record this time delay
						if(term.getType().contains("interval")){
							indexDelay.add(i);
							delay += Integer.valueOf(term.getDuration());
							
							state = 1;
						}
						// set pre to this event term
						else if(term.getType().contains("event")){
							pre = term;
							
							state = 2;
						}
						break;
					}
					// previous term is a time delay
					case 1:{
						// accumulate this time delay
						if(term.getType().contains("interval")){
							indexDelay.add(i);
							delay += Integer.valueOf(term.getDuration());
							
							state = 1;
						}
						// create a time delay entry, reset pre to this event term
						else if(term.getType().contains("event")){
							TimeDelayEntry entry = new TimeDelayEntry(pre, term, String.valueOf(delay));
							entries.add(entry);
							
								if(pre != null){
									System.out.println("Time delay entry: "+pre.getType()+" "+term.getType()+" "+delay);
								}
								else{
									System.out.println("Time delay entry: null "+term.getType()+" "+delay);
								}
								
							delay = 0;
							pre = term;
							
							state = 2;
						}
						break;
					}
					// previous term is an event
					case 2:{
						// record this time delay
						if(term.getType().contains("interval")){
							indexDelay.add(i);
							delay += Integer.valueOf(term.getDuration());
							
							state = 1;
						}
						// reset pre to this event term
						else if(term.getType().contains("event")){
							pre = term;
							
							state = 2;
						}
						break;
					}
				}
			}
			
			// the last term in SEQ CLAUSE is time delay, create a time delay entry
			if(state == 1){
				TimeDelayEntry entry = new TimeDelayEntry(pre, null, delay);
				entries.add(entry);
					System.out.println("Time delay entry: "+pre.getName()+" null "+delay);
				
			}
			
			// remove all time delays in the SEQ CLAUSE
			int index;
			for(int i = 0; i < indexDelay.size(); i++){
				index = indexDelay.get(i);
				terms.remove(index-i);
			}
		}*/
		
		/*
		 * (SEQ CLAUSE (or SEQ CLAUSE)*) = ( SEQ CLAUSE (and SEQ CLAUSE)+ )
		 * 
		 * 
		 * @param singleTerms only used for passing data
		 * @param timeTerms only used for passing data
		 * @param seqTerms only used for passing data
		 */
		/*private OrClause transformAndClause(List<Term> singleTerms, List<Term> timeTerms, List<SeqClause> seqTerms){
			
			OrClause ret = new OrClause();
			// the sequence into which new terms are inserted
			List<SeqTerms> seqs = new ArrayList<SeqTerms>();
			
			// insert single terms in AND CLAUSE
			int singleSize = singleTerms.size();
			//XXX check n should not be too big
			if(singleSize > 0){
				int factorial = factorial(singleSize);
				int [][] permu = new int [factorial][singleSize];
				int [] index = new int [singleSize];
				for(int i = 0; i < singleSize; i++){
					index[i] = i;
				}
				permutation(index, singleSize, permu, 0);
				
				// every seq constituted by single terms
				for(int i = 0; i < factorial; i++){
					int [] seqIndex = permu[i];
					
					SeqClause strs = new SeqClause();
					for(int j = 0; j < singleSize; j++){
						strs.addTerm(singleTerms.get(seqIndex[j]));
					}
					seqs.add(new SeqTerms(0, strs));
				}
			}
			
			// insert seq terms in AND CLAUSE
			for(int i = 0; i < seqTerms.size(); i++){
				SeqClause iseq = seqTerms.get(i);
				
				seqs = insertSeq(seqs, iseq);
			}
			
			// create result
			for(int i = 0; i < seqs.size(); i++){
				AndClause atrs = new AndClause();
				atrs.addSeqClause(seqs.get(i).getSequence());
				ret.addSeqClause(seqs.get(i).getSequence());
			}
			
			//test output
			
			List<AndTerms> lat = ret.getTerms();
			for(int i = 0; i < lat.size(); i++){
				AndTerms at = lat.get(i);
				
				System.out.println();
				SeqTerms st= at.getTerms().get(0);
				List<Term> ts = st.getTerms();
				System.out.println();
				for(int j = 0; j < ts.size(); j++){
					Term t = ts.get(j);
					System.out.print(t.getType()+" ");
				}
			}
			System.out.println("\nsize: "+lat.size());
			
			return ret;
		}
		
		private int factorial(int n){
			//TODO check n
			if(n < 6){
				for(int i = n-1; i > 0; i--){
					n = n*i;
				}
				return n;
			}
			else
				return 0;
		}
		
		private int permutation(int [] a, int n, int[][] result, int size){
			if (n == 1) {
	            int [] copy = new int [a.length];
	            System.arraycopy(a, 0, copy, 0, a.length);
	            result[size] = copy;
	            
	            return size+1;
	        }
			else{
				for (int i = 0; i < n; i++) {
		            swap(a, i, n-1);
		            size = permutation(a, n-1, result, size);
		            swap(a, i, n-1);
				}
				return size;
			}
		}
		
		private void swap(int [] a, int i, int j){
			int c;
			c = a[i];
			a[i] = a[j];
			a[j] = c;
		}*/
		
		
		private class SequenceOption{
			private int insertIndex;
			
			private final SeqClause sequence;
			
			public SequenceOption(int i, SeqClause seq){
				insertIndex = i;
				sequence = seq;
			}
			
			public int getInsertIndex(){
				return insertIndex;
			}
			
			public void setInsertIndex(int i){
				insertIndex = i;
			}
			
			public SeqClause getSequence(){
				return sequence;
			}
		}
		
	}

	
}
