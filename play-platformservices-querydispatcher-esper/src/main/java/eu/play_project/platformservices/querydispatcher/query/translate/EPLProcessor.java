/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.openrdf.query.parser.sparql.ast.ASTA;
import org.openrdf.query.parser.sparql.ast.ASTB;
import org.openrdf.query.parser.sparql.ast.ASTC;
import org.openrdf.query.parser.sparql.ast.ASTEventClause;
import org.openrdf.query.parser.sparql.ast.ASTEventGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTEventPattern;
import org.openrdf.query.parser.sparql.ast.ASTNotClause;
import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;
import org.openrdf.query.parser.sparql.ast.ASTTimeBasedEvent;
import org.openrdf.query.parser.sparql.ast.Node;
import org.openrdf.query.parser.sparql.ast.SimpleNode;
import org.openrdf.query.parser.sparql.ast.Token;
import org.openrdf.query.parser.sparql.ast.VisitorException;
import org.openrdf.query.MalformedQueryException;

import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.querydispatcher.query.translate.util.BDPLTranslateException;
import eu.play_project.platformservices.querydispatcher.query.translate.util.BDPLTranslateUtil;
import eu.play_project.platformservices.querydispatcher.query.translate.util.EPLConstants;
import eu.play_project.platformservices.querydispatcher.query.translate.util.IEntry;
import eu.play_project.platformservices.querydispatcher.query.translate.util.NotEntry;
import eu.play_project.platformservices.querydispatcher.query.translate.util.NotTable;
import eu.play_project.platformservices.querydispatcher.query.translate.util.OrClause;
import eu.play_project.platformservices.querydispatcher.query.translate.util.SeqClause;
import eu.play_project.platformservices.querydispatcher.query.translate.util.Term;
import eu.play_project.platformservices.querydispatcher.query.translate.util.TimeDelayEntry;
import eu.play_project.platformservices.querydispatcher.query.translate.util.TimeDelayTable;



/**
 * Processes the part of real time query, and translates it into a EPL for Esper.
 * 
 * @author ningyuan
 *
 */
public class EPLProcessor {
	
	public static void process(ASTOperationContainer qc)
			throws MalformedQueryException{
		EPLTranslator translator = new EPLTranslator();
		
		EPLTranslatorData data = new EPLTranslatorData();
		
		try {
			qc.jjtAccept(translator, data);
		} catch (VisitorException e) {
			e.printStackTrace();
		}
	}
	
	private static class EPLTranslatorData{
		
		private NotTable notTable = new NotTable();
		
		private StringBuffer sparqlText = new StringBuffer();
		
		public NotTable getNotTable(){
			return notTable;
		}
		
		public StringBuffer getSparqlText(){
			return sparqlText;
		}
	}
	
	private static class EPLTranslator extends ASTVisitorBase {
		
		private static int MAX_NUM_SEQ_CLAUSE = 24;
		
		@Override
		public Object visit(ASTEventPattern node, Object data)
			throws VisitorException
		{
			
			OrClause ret;
			
			// EventPattern = C
			if(node.jjtGetNumChildren() <= 1){
				ret = (OrClause) super.visit(node, data);
				
				//test output
				if(node.getTop())
				{
					try{
						printExpression(ret, (EPLTranslatorData)data);
					}catch (BDPLTranslateException e) {
						throw new VisitorException(e.getMessage());
					}
				}
				
				return ret;
			}
			// EventPattern = C (seq C)+
			else{
				List<OrClause> expressions = new ArrayList<OrClause>();
				
				for(Node child : node.jjtGetChildren()){
					expressions.add((OrClause)child.jjtAccept(this, data));
				}
				
				// return the united expression
				try {
					ret =  unionSeqExpression(expressions);
				} catch (BDPLTranslateException e) {
					throw new VisitorException(e.getMessage());
				}
				
				//test output
				if(node.getTop())
				{
					try{
						printExpression(ret, (EPLTranslatorData)data);
					}catch (BDPLTranslateException e) {
						throw new VisitorException(e.getMessage());
					}
				}
				
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
				} catch (BDPLTranslateException e) {
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
			if(BDPLTranslateUtil.getTermType(notTerms[1]) == BDPLTranslateUtil.TERM_TIME){
				// C is time delay
				if(BDPLTranslateUtil.getTermType(notTerms[2]) == BDPLTranslateUtil.TERM_TIME){
					// B is longer than C
					if(notTerms[1].getDuration() > notTerms[2].getDuration()){
						// A is time delay
						// T -> T
						if(BDPLTranslateUtil.getTermType(notTerms[0]) == BDPLTranslateUtil.TERM_TIME){
							notTerms[0].setDuration(notTerms[0].getDuration()+notTerms[2].getDuration());
							
							List<Term> seq = expression.getSeqClauses().get(0).getTerms();
							seq.clear();
							seq.add(notTerms[0]);
							
							return expression;
						}
						// A is event
						// A -> T
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
					// may fix time delay
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
				// may fix time delay
				seq.add(notTerms[0]);
				seq.add(notTerms[2]);
				
				return expression;
			}
		}
		
		@Override
		public Object visit(ASTTimeBasedEvent node, Object data)
				throws VisitorException
		{
			/*System.out.print(" "+node.getOperator()+" ");
			System.out.print(" "+node.getEventName());
			System.out.print("("+node.getEventParam()+") ");
			
			super.visit(node, data);
			
			return data;*/
			
			super.visit(node, data);
			
			Term term = new Term(node.getEventName());
			
			try {
				term.setDuration(BDPLTranslateUtil.getDurationInSec(node.getDuration()));
			} catch (BDPLTranslateException e) {
				throw new VisitorException("Time delay format exception: "+node.getDuration());
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
			 * Get the sparql text of this event 
			 */
			StringBuffer sparqlText = ((EPLTranslatorData)data).getSparqlText();
			Token token = node.jjtGetFirstToken();
			for(int i = 0; i < 3; i++){
				token = token.next;
			}
			for(; token != node.jjtGetLastToken(); token = token.next){
				sparqlText.append(token.image+" ");
			}
			// last token must be }
				System.out.println("\nSparql Text: "+sparqlText.toString());
			
			/*
			 * Create the term of this event
			 */
			OrClause expression;
			try{
				String [] info = processSparql(sparqlText.toString());
			
				Term term = new Term(info[0]);
				term.setSparqlText(sparqlText.toString());
				term.setVar(info[1]);
				sparqlText.delete(0, sparqlText.length());
				
				SeqClause seq = new SeqClause();
				seq.addTerm(term);
				
				expression = new OrClause();
				expression.addSeqClause(seq);
			}
			catch(BDPLTranslateException e){
				throw new VisitorException(e.getMessage());
			}
			
			return expression;
		}
		
		@Override
		public Object visit(ASTEventGraphPattern node, Object data)
				throws VisitorException
		{
			Object ret = super.visit(node, data);
			
			//TODO get event type
			
			return ret;
		}
		
		//TODO 
		private String[] processSparql(String sparql) throws BDPLTranslateException{
			// ret[0]: event rdf:type
			// ret[1]: the first variable
			String [] ret = new String [2];
			
			int i1 = -1, i2 = -1;
			
			i1 = sparql.indexOf("rdf:type");
			if(i1 == -1){
				throw new BDPLTranslateException("An Event dose not have property rdf:type");
			}
			else{
				i2 = sparql.indexOf(".");
				
				if(i2 > i1){
					ret[0] = sparql.substring(i1+8, i2).trim();
				}
				else{
					throw new BDPLTranslateException("Could not determine the end of property rdf:type");
				}
			}
			
			i1 = sparql.indexOf("?");
			if(i1 == -1){
				i1 = sparql.indexOf("$");
				if(i1 == -1){
					throw new BDPLTranslateException("Could not determine a variable in an event");
				}
			}
			
			i2 = i1;
			char c = sparql.charAt(i2);
			while(i2 < sparql.length() && c != ' ' && c != '\r' && c != '\n'){
				c = sparql.charAt(i2++);
			}
			ret[1] = sparql.substring(i1, i2);
			
			return ret;
		}
		
		/*
		 * Exp = Exp (or Exp)+
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
		 * Exp = ((TERM (seq TERM)*:=SEQ CLAUSE) or SEQ CLAUSE)*:=OR CLAUSE)
		 */
		
		
		/*
		 * Exp = Exp (seq Exp)+
		 * 
		 * @param expressions 
		 * @return united expression
		 */
		private OrClause unionSeqExpression(List<OrClause> expressions) throws BDPLTranslateException{
			
			OrClause ret = new OrClause();
			combineSeqClause(new ArrayList<SeqClause>(), expressions, 1, ret);
			
			return ret;
		}
		
		/*
		 * Exp = Exp (and Exp)+
		 */
		private OrClause unionAndExpression(List<OrClause> expansions) throws BDPLTranslateException{
			OrClause ret = new OrClause();
			combineSeqClause(new ArrayList<SeqClause>(), expansions, 2, ret);
			
			return ret;
		}
		
		/*
		 * (SEQ CLAUSE (op SEQ CLAUSE)*):=combi (or (SEQ CLAUSE (op SEQ CLAUSE)*))* = ( SEQ CLAUSE (or SEQ CLAUSE)*):=exp (op (SEQ CLAUSE (or SEQ CLAUSE)*))*
		 * 
		 * @param combi a combination of and terms from each exps
		 * @param exps 
		 */
		private void combineSeqClause(List<SeqClause> combi, List<OrClause> exps, int type, OrClause result) throws BDPLTranslateException{
			int size = exps.size();
			
			if(size > 0){
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
								// (SEQ CLAUSE (seq SEQ CLAUSE)*) (or (SEQ CLAUSE (seq SEQ CLAUSE)*)) = (SEQ CLAUSE (or SEQ CLAUSE)*)(seq (SEQ CLAUSE (or SEQ CLAUSE)*))*
								expand1(combi, result);
								break;
							}
							case 2:{
								// SEQ CLAUSE (or SEQ CLAUSE)* = (SEQ CLAUSE (or SEQ CLAUSE)*)(and (SEQ CLAUSE (or SEQ CLAUSE)*))*
								expand2(combi, result);
								break;
							}
						}
						
						// remove the last SEQ CLAUSE
						combi.remove(combi.size()-1);
						pointer --;
					}
					// a further SEQ CLAUSE will be chosen here
					else{
						int index = stack[pointer];
						List<SeqClause> seqcs = exps.get(pointer).getSeqClauses();
						
						// chose a SEQ CLAUSE from this expression
						// pointer goes to the next expression
						if(index < seqcs.size()){
							combi.add(seqcs.get(index));
							stack[pointer] = index + 1;
							pointer++;
							
						}
						// all SEQ CLAUSE from this expression are chosen
						// pointer goes back to the previous expression
						else{
							if(pointer > 0){
								stack[pointer] = 0;
								combi.remove(combi.size()-1);
							}
							pointer --;		
						}
						
					}
				}
			}
			
		}
		
		/*
		 * (AND CLAUSE (op AND CLAUSE)*):=combi (or (AND CLAUSE (op AND CLAUSE)*))* = ( AND CLAUSE (or AND CLAUSE)*):=exp (op (AND CLAUSE (or AND CLAUSE)*))*
		 * 
		 * @param combi a combination of and terms from each exps
		 * @param exps 
		 */
		private void combineSeqClauseRecursive(List<SeqClause> combi, List<OrClause> exps, int depth, int type, OrClause result) throws BDPLTranslateException{
			
			if(exps.size() > 0){
				// make combination of and terms from every expansion
				if(depth < exps.size()){
					OrClause otrs = exps.get(depth);
					
					for(int i = 0; i < otrs.getSeqClauses().size(); i++){
						SeqClause seqc = otrs.getSeqClauses().get(i);
						// add the next and terms in this level
						combi.add(seqc);
						// go to the next depth
						combineSeqClauseRecursive(combi, exps, depth+1, type, result);
						// remove the old and terms in this level
						combi.remove(combi.size()-1);
					}
				}
				else{
					switch(type){
						case 1:{
							// (SEQ CLAUSE (seq SEQ CLAUSE)*) (or (SEQ CLAUSE (seq SEQ CLAUSE)*)) = (SEQ CLAUSE (or SEQ CLAUSE)*)(seq (SEQ CLAUSE (or SEQ CLAUSE)*))*
							expand1(combi, result);
							break;
						}
						case 2:{
							// AND CLAUSE (or AND CLAUSE)* = (AND CLAUSE (or AND CLAUSE)*)(and (AND CLAUSE (or AND CLAUSE)*))*
							expand2(combi, result);
							break;
						}
					}
				}
			}
		}
		

		/*
		 * SEQ CLAUSE = (SEQ CLAUSE (seq SEQ CLAUSE)*):= combi
		 * 
		 * @param combi only used for passing data, must not be null
		 */
		private void expand1(List<SeqClause> combi, OrClause result) throws BDPLTranslateException{
			
			// create new result SEQ CLAUSE and its time delay table
			SeqClause seq = new SeqClause();
			List<Term> trs = seq.getTerms();
			TimeDelayTable tdTable = new TimeDelayTable();
			seq.setTdTable(tdTable);
			
			Term endEvent = null;
			for(int i = 0; i < combi.size(); i++){
				
				// set the end event from last SEQ CLAUSE
				if(trs.size() > 0){
					endEvent = trs.get(trs.size()-1);
				}
				
				SeqClause current = combi.get(i);
				List<Term> cts = current.getTerms();
				// initiate SEQ CLAUSE with only one term
				if(current.getTdTable() == null){
					initTimeDelayTable(current);
				}
				
				// copy time delay table of the current term for further connection
				TimeDelayTable currentTable = new TimeDelayTable();
				List<TimeDelayEntry> currentEntries = current.getTdTable().getEntries();
				for(int j = 0; j < currentEntries.size(); j++){
					TimeDelayEntry currentEntry = currentEntries.get(j);
					currentTable.getEntries().add(new TimeDelayEntry(currentEntry.getStart(), currentEntry.getEnd(), currentEntry.getDuration()));
				}
					
					System.out.println("\nConnected");
					for(int j = 0; j < trs.size(); j++){
						System.out.print(trs.get(j).getName()+" ");
					}
					System.out.print("+ ");
					for(int j = 0; j < cts.size(); j++){
						System.out.print(cts.get(j).getName()+" ");
					}
					System.out.println();
				
				appendSeqClauses(endEvent, trs, tdTable, cts, currentTable);
					
					List<TimeDelayEntry> temp = tdTable.getEntries();
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
			result.addSeqClause(seq);
		}
		
		/*
		 *  Exp = (SEQ CLAUSE (and SEQ CLAUSE)*):= combi
		 */
		private void expand2(List<SeqClause> combi, OrClause result) throws BDPLTranslateException{
			
			OrClause or;
			
			// (SEQ CLAUSE (or SEQ CLAUSE)*) = AND CLAUSE
			or = transformAndClause(combi);
			
			List<SeqClause> seqcs = or.getSeqClauses();
			for(int j = 0; j < seqcs.size(); j++){
				result.addSeqClause(seqcs.get(j));
			}			
		}
		
		/*
		 * 
		 * @param seqcs must not be null
		 */
		private OrClause transformAndClause(List<SeqClause> seqcs) throws BDPLTranslateException{
			OrClause ret = new OrClause();
			
			if(seqcs.size() > 0){
				
				List<TimeDelayEntry> entries = new ArrayList<TimeDelayEntry>();
			
				// every SEQ CLAUSE in this AND CLAUSE 
				for(int i = 0; i < seqcs.size(); i++){
					SeqClause st = seqcs.get(i);
					
					if(st.getTdTable() == null){
						initTimeDelayTable(st);
					}
					TimeDelayTable temp = st.getTdTable();
					
					List<Term> terms = st.getTerms();
					if(terms.size() < 1){
						throw new BDPLTranslateException("Time delay should not be an operant of an AND operator");
					}
					else{
						for(int j = 0; j < terms.size(); j++){
							if(BDPLTranslateUtil.getTermType(terms.get(j)) == BDPLTranslateUtil.TERM_TIME){
								throw new BDPLTranslateException("Time delays connected with AND could not be expanded any more");
							}
						}
						
						// make the time delay table of the result
						for(TimeDelayEntry entry : temp.getEntries()){
							entries.add(entry);
						}
					}
				}
				
				
				
			
				List<SeqTerms> seqs = new ArrayList<SeqTerms>();
				SeqClause copy = new SeqClause();
				List<Term> init = seqcs.get(0).getTerms();
				for(int i = 0; i < init.size(); i++){
					copy.addTerm(init.get(i));
				}
				
				seqs.add(new SeqTerms(0, copy));
				
					/*System.out.println("START AND ELE:");
					SeqClause s = seqcs.get(0);
					for(Term ter : s.getTerms()){
						System.out.println(ter.getType());
					}*/
				
				// insert seq terms in AND CLAUSE
				for(int i = 1; i < seqcs.size(); i++){
					SeqClause iseq = seqcs.get(i);
						
						/*System.out.println("AND ELE:");
						s = seqcs.get(i);
						for(Term ter : s.getTerms()){
							System.out.println(ter.getType());
						}*/
					seqs = insertSeq(seqs, iseq);
				}
				
				// create result
				for(int i = 0; i < seqs.size(); i++){
					SeqClause seqc = seqs.get(i).getSequence();
					
					// copy time delay table for every sequence combinations
					TimeDelayTable tdTable = new TimeDelayTable();
					List<TimeDelayEntry> temp = tdTable.getEntries();
					for(int j = 0; j < entries.size(); j++){
						TimeDelayEntry entry = entries.get(j);
						temp.add(new TimeDelayEntry(entry.getStart(), entry.getEnd(), entry.getDuration()));
					}
					
					// fix time delay at start
					List<TimeDelayEntry> left = tdTable.getEntriesByStart(null);
					BDPLTranslateUtil.reduceStartDelayEntry(left, seqc, true, tdTable);
					/*
					 * ([T] -> A -> *) -> * to (T -> A -> *)
					 */
					if(left.size() > 1){
						List<Term> terms = seqc.getTerms();
						if(left.get(0).getEnd() != terms.get(0)){
							throw new BDPLTranslateException("Fixed Time delay error at start");
						}
						else{
							Term term = new Term(EPLConstants.TIMER_INTERVAL);
							term.setDuration(left.get(0).getDuration());
							terms.add(0, term);
								
							tdTable.getEntries().remove(left.get(0));
						}
					}
					
					// fix time delay at end
					List<TimeDelayEntry> right = tdTable.getEntriesByEnd(null);
					BDPLTranslateUtil.reduceEndDelayEntry(right, seqc, true, tdTable);
					/*
					 * * -> (* -> A -> [T]) to * -> (* -> A -> T)
					 */
					if(right.size() > 1){
						List<Term> terms = seqc.getTerms();
						if(right.get(right.size()-1).getStart() != terms.get(terms.size()-1)){
							throw new BDPLTranslateException("Fixed Time delay error at end");
						}
						else{
							Term term = new Term(EPLConstants.TIMER_INTERVAL);
							term.setDuration(right.get(right.size()-1).getDuration());
							terms.add(term);
								
							tdTable.getEntries().remove(right.get(right.size()-1));
						}
					}
					
					seqc.setTdTable(tdTable);
					ret.addSeqClause(seqc);
				}
			}
			
			return ret;
		}
		
		/*
		 * Create new time delay table and time delay entries for a SEQ CLAUSE containing only one term.
		 */
		private void initTimeDelayTable(SeqClause seqc) throws BDPLTranslateException{
			List<Term> terms = seqc.getTerms();
			
			if(terms.size() != 1){
				throw new BDPLTranslateException("Time delay table can not be initiated");
			}
			
			Term term = terms.get(0);
			
			// create a new time delay table and set it to the SEQ CLAUSE
			TimeDelayTable tdTable = new TimeDelayTable();
			seqc.setTdTable(tdTable);
			
			// term is time delay: create one time delay entry, remove time delay term
			if(BDPLTranslateUtil.getTermType(term) == BDPLTranslateUtil.TERM_TIME){
				List<TimeDelayEntry> entries = tdTable.getEntries();
				entries.add(new TimeDelayEntry(null, null, term.getDuration()));
				terms.remove(0);
			}
			
			// term is event: empty table
		}
		
		/*
		 * Append a SEQ CLAUSE and its time delay table 
		 *
		 * @param ct content should not be changed
		 */
		private void appendSeqClauses(Term endEvent, List<Term> rt, TimeDelayTable resultTable, List<Term> ct, TimeDelayTable currentTable){
			

			Term startEvent = null; 
			if(ct.size() > 0){
				startEvent = ct.get(0);
			}
			
			List<TimeDelayEntry> end = resultTable.getEntriesByEnd(null);
			
			List<TimeDelayEntry> left = currentTable.getEntriesByStart(null);
			
			
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
				System.out.println("leftSize "+left.size());
			
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
					if(BDPLTranslateUtil.getTermType(endEvent) == BDPLTranslateUtil.TERM_TIME){
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
						
							if(BDPLTranslateUtil.getTermType(startEvent) == BDPLTranslateUtil.TERM_EVENT){
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
					if(BDPLTranslateUtil.getTermType(endEvent) == BDPLTranslateUtil.TERM_TIME){
							
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
							
								if(BDPLTranslateUtil.getTermType(startEvent) == BDPLTranslateUtil.TERM_EVENT){
									
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
		private List<SeqTerms> insertSeq(List<SeqTerms> seqs, SeqClause iseq) throws BDPLTranslateException{
			List<SeqTerms> ret = new ArrayList<SeqTerms>();
			
			List<SeqTerms> temp = new ArrayList<SeqTerms>();
			List<Term> iterms = iseq.getTerms();
			
			if(seqs.size() > 0){
				// every sequence into which the iseq should be inserted
				for(int i = 0; i < seqs.size(); i++){
					SeqTerms seq = seqs.get(i);
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
							throw new BDPLTranslateException("The number of SEQ Clause is larger than "+MAX_NUM_SEQ_CLAUSE);
						}
						
						ret.add(temp.get(j));
					}
					temp.clear();
				}
			}
			// when seqs is empty sequences, copy iseq as sequence
			else{
				ret.add(new SeqTerms(0, iseq));
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
		private List<SeqTerms> insertTerm(List<SeqTerms> seqs, Term term){
			
			List<SeqTerms> ret = new ArrayList<SeqTerms>();
			
			// every sequence into which the term should be inserted
			for(int i = 0; i < seqs.size(); i++){
				SeqTerms seq = seqs.get(i);
				int index = seq.getInsertIndex();
				SeqClause ostrs = seq.getSequence();
				List<Term> otrs = ostrs.getTerms();
				
					/*//test output
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
					SeqTerms instrs = new SeqTerms(j+1, strs);
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
		
		private void printExpression(OrClause result, EPLTranslatorData data) throws BDPLTranslateException{
			
			NotTable notTable = data.getNotTable();
			
			List<SeqClause> seqcs = result.getSeqClauses();
			
			if(seqcs.size() > 0){
				
				System.out.println("\nSeq clause size: "+seqcs.size());
				
				for(int i = 0; i < seqcs.size(); i++){
					System.out.println("TimeDelayTable "+i);
					TimeDelayTable tdTable = seqcs.get(i).getTdTable();
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
				
				printSeqClause(seqcs.get(0), notTable, 0, eIndexs, nIndexs);
			
				for(int i = 1; i < seqcs.size(); i++){
					System.out.print(EPLConstants.OPERATOR_OR+" ");
					
					printSeqClause(seqcs.get(i), notTable, i, eIndexs, nIndexs);
					
				}
				
			}
		}
		
		private void printSeqClause(SeqClause seqc, NotTable notTable, int sIndex, int [] eIndexs, int [] nIndexs) throws BDPLTranslateException{
			List<Term> ltrs = seqc.getTerms();
			Map<Term, Term> notList = new HashMap<Term, Term>();
			TimeDelayTable tdTable = seqc.getTdTable();
			Stack<IEntry> openParaStack = new Stack<IEntry>();
			IEntry stackTop = null;
			StringBuffer sparqlText = new StringBuffer();
			// event tag and not tag
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
			
			System.out.print("( ");
			
			if(ltrs.size() > 0){
				StringBuffer eParams = new StringBuffer();
				
				// only one term without being unioned by seq
				if(tdTable == null){
					if(ltrs.size() > 1){
						throw new BDPLTranslateException("Sequence clause has no time delay table");
					}
					else{
						Term term = ltrs.get(0);
						System.out.print(EPLConstants.EVERY+" ");
						
						if(BDPLTranslateUtil.getTermType(term) == BDPLTranslateUtil.TERM_TIME){
							System.out.print(EPLConstants.TIMER_INTERVAL+"("+term.getDuration()+") ");
						}
						else{
							System.out.print(EPLConstants.EVENTTAG+eIndex+"=");
							eIndexs[sIndex] = eIndex;
							eIndex++;
							
							for(int i = eStart; i < eIndex; i++){
								eParams.append(","+EPLConstants.EVENTTAG+i);
							}
							
							sparqlText.append(term.getSparqlText());
							
							System.out.print(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, String.format(EPLConstants.SPARQL_QUERY, term.getVar(), sparqlText), eParams.toString())+") ");
							
							eParams.delete(0, eParams.length());
						}
					}
				}
				
				/*
				 *              A         A  
				 *         T -> T         T -> T                        
				 *         A -> T         T -> A 
				 *            [T]    +    [T]
				 *        A -> T)         (T -> A  
				 * B -> (A -> T))         ((T -> A) -> B  
				 * 
				 */
				else{
					// ( : time delay entries staring from null
					List<TimeDelayEntry> left = tdTable.getEntriesByStart(null);
					BDPLTranslateUtil.sortTimeDelayEntryByEnd(left, seqc);
					//BDPLTransformerUtil.reduceStartDelayEntry(left, seqc, true, tdTable);
					
					for(int i = left.size()-1; i > -1 ; i--){
						System.out.print("( ");
						openParaStack.push(left.get(i));
						stackTop = openParaStack.peek();
					}
					
					// the first term
					Term term = ltrs.get(0);
					
					// ) -> term : time delay entries ending at term
					List<TimeDelayEntry> right = tdTable.getEntriesByEnd(term);
					BDPLTranslateUtil.sortTimeDelayEntryByStart(right, seqc);
					
					String every = EPLConstants.EVERY+" ";
					if(right.size() <= 1){
						if(right.size() == 1){
							if(right.get(0) == stackTop){
								every = "";
								
								System.out.print(EPLConstants.EVERY+" ");
								System.out.print(EPLConstants.TIMER_INTERVAL+"("+right.get(0).getDuration()+") ) ");
								System.out.print(EPLConstants.OPERATOR_SEQ+" ");
								
								openParaStack.pop();
								if(openParaStack.size() > 0){
									stackTop = openParaStack.peek();
								}
								else{
									stackTop = null;
								}
							}
							else{
								throw new BDPLTranslateException("Time delays are overlapping");
							}
						}
					}
					else{
						throw new BDPLTranslateException("Time delays are duplicated at begin");
					}
					
					boolean lastTermTime = false;
					System.out.print(every);
					if(BDPLTranslateUtil.getTermType(term) == BDPLTranslateUtil.TERM_TIME){
						System.out.print(EPLConstants.TIMER_INTERVAL+"("+term.getDuration()+") ");
						lastTermTime = true;
					}
					else{
						System.out.print(EPLConstants.EVENTTAG+eIndex+"=");
						eIndexs[sIndex] = eIndex;
						eIndex++;
						
						for(int i = eStart; i < eIndex; i++){
							eParams.append(","+EPLConstants.EVENTTAG+i);
						}
						
						sparqlText.append(term.getSparqlText());
						System.out.print(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, String.format(EPLConstants.SPARQL_QUERY, term.getVar(), sparqlText), eParams.toString())+") ");
						
						eParams.delete(0, eParams.length());
					}
					
					
					// register not list
					boolean notOpenPara = false;
					NotEntry entry = notTable.getEntryByNotStart(term);
					if(entry != null){
						if(BDPLTranslateUtil.getTermType(entry.getNot()) == BDPLTranslateUtil.TERM_TIME){
							openParaStack.push(entry);
							stackTop = openParaStack.peek();
							notOpenPara = true;
						}
						else{
							// put only not event in list
							notList.put(entry.getNotEnd(), entry.getNot());
						}
					}
					
					
					
					boolean thisTermTime = false;
					for(int i = 1; i < ltrs.size(); i++){
						// last term
						Term lTerm = term;
						// term
						term = ltrs.get(i);
						
						
						// last term -> ( term : time delay entries starting from last term
						left = tdTable.getEntriesByStart(lTerm);
						BDPLTranslateUtil.sortTimeDelayEntryByEnd(left, seqc);
						//BDPLTransformerUtil.reduceStartDelayEntry(left, seqc, true, tdTable);
							
							/*for(int j = left.size()-1; j >-1; j--){
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
						
						
						// last term ) -> term : time delay entries ending at this term
						right = tdTable.getEntriesByEnd(term);
						BDPLTranslateUtil.sortTimeDelayEntryByStart(right, seqc);
							
							/*for(int j = 0; j < right.size(); j++){
								System.out.println(term.getName()+" end "+right.get(j).getStart().getName());
							}*/
						
						// time delay after last term
						String interval = null;
						if(left.size() > 0){
							if(right.size() > 0 && right.get(right.size()-1) == left.get(0)){
								lastTermTime = true;
								
								interval = EPLConstants.OPERATOR_SEQ+" "+EPLConstants.TIMER_INTERVAL+"("+left.get(0).getDuration()+") ";
								//interval = "-> interval "+left.get(0).getDuration()+" ";
								
								for(Term not : notList.values()){
									interval += EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+EPLConstants.NOTEVENTTAG+nIndex+"="+not.getName();
									
									
									for(int j = eStart; j < eIndex; j++){
										eParams.append(","+EPLConstants.EVENTTAG+j);
									}
									
									int se = sparqlText.length();
									sparqlText.append(not.getSparqlText());
									interval += ("("+String.format(EPLConstants.FILTER_RDF, String.format(EPLConstants.SPARQL_QUERY, not.getVar(), sparqlText), eParams.toString()+","+EPLConstants.NOTEVENTTAG+nIndex)+") ");
									sparqlText.delete(se, sparqlText.length());
									
									nIndexs[sIndex] = nIndex;
									nIndex++;
									
									eParams.delete(0, eParams.length());
									//interval += "and not "+not.getName()+" ";
								}
								
								right.remove(right.size()-1);
								left.remove(0);
							}
						}
						
						/*
						 *  Close parenthesis after last term
						 */
						boolean flag1 = false;
						for(int j = 0; j < right.size(); j++){
							TimeDelayEntry temp = right.get(j);
							if(temp == stackTop){
								if(notOpenPara){
									throw new BDPLTranslateException("Not time delay can not start from a time delay");
								}
								
								flag1 = true;
								System.out.print(") "+EPLConstants.OPERATOR_AND+" "+EPLConstants.TIMER_INTERVAL+"("+temp.getDuration()+") ");
								//System.out.print(") and interval "+temp.getDuration()+" ");
								openParaStack.pop();
								if(openParaStack.size() > 0){
									stackTop = openParaStack.peek();
								}
								else{
									stackTop = null;
								}
							}
							else{
								throw new BDPLTranslateException("Time delays are overlapping");
							}
						}
						
						/*
						 * Time Delay between last and this term
						 */
						if(interval != null){
							System.out.print(interval);
						}
						
						System.out.print(EPLConstants.OPERATOR_SEQ+" ");
						
						/*
						 * Open paras on the right of this term ( Time delay starting from last term )
						 */
						boolean flag2 = false;
						for(int j = left.size()-1; j > -1 ; j--){
							flag2 = true;
							System.out.print("( ");
							openParaStack.push(left.get(j));
							stackTop = openParaStack.peek();
						}
						
						/*
						 * Open paras on the right of this term ( Not time starting from this term)
						 */
						if(notOpenPara){
							System.out.print("( ");
						}
						
						/*
						 *  This Term
						 */
						if(BDPLTranslateUtil.getTermType(term) == BDPLTranslateUtil.TERM_TIME){
							System.out.print(EPLConstants.TIMER_INTERVAL+"("+term.getDuration()+") ");
							//System.out.print("interval "+term.getDuration()+" ");
							thisTermTime = true;
						}
						else{
							System.out.print(EPLConstants.EVENTTAG+eIndex+"=");
							eIndexs[sIndex] = eIndex;
							eIndex++;
							
							
							for(int j = eStart; j < eIndex; j++){
								eParams.append(","+EPLConstants.EVENTTAG+j);
							}
							
							sparqlText.append(term.getSparqlText());
							System.out.print(term.getName()+"("+String.format(EPLConstants.FILTER_RDF, String.format(EPLConstants.SPARQL_QUERY, term.getVar(), sparqlText), eParams.toString())+") ");
							
							eParams.delete(0, eParams.length());
						}
						
						//XXX check
						if(!lastTermTime && !thisTermTime){
							if(flag1 && flag2){
								throw new BDPLTranslateException("Time delays are overlapping");
							}
						}
						lastTermTime = thisTermTime;
						
						
						/*
						 * Not Event of this term
						 */
						for(Term not : notList.values()){
							System.out.print(EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+EPLConstants.NOTEVENTTAG+nIndex+"="+not.getName());
							
							if(thisTermTime){
								for(int j = eStart; j < eIndex; j++){
									eParams.append(","+EPLConstants.EVENTTAG+j);
								}
							}
							else{
								for(int j = eStart; j < eIndex-1; j++){
									eParams.append(","+EPLConstants.EVENTTAG+j);
								}
							}
							
							int se = sparqlText.length();
							sparqlText.append(not.getSparqlText());
							System.out.print("("+String.format(EPLConstants.FILTER_RDF, String.format(EPLConstants.SPARQL_QUERY, not.getVar(), sparqlText), eParams.toString()+","+EPLConstants.NOTEVENTTAG+nIndex)+") ");
							sparqlText.delete(se, sparqlText.length());
							
							nIndexs[sIndex] = nIndex;
							nIndex++;
							
							eParams.delete(0, eParams.length());
							
							//System.out.print("and not "+not.getName()+" ");
						}
						
						if(notList.get(term) != null){
							// not list should be empty, after processing every term!
							notList.remove(term);
						}
						
						/*
						 * Not Time of this event
						 */
						NotEntry notTime = notTable.getEntryByNotEnd(term);
						if(notTime != null){
							if(BDPLTranslateUtil.getTermType(notTime.getNot()) == BDPLTranslateUtil.TERM_TIME){
								if(notTime == stackTop){
									System.out.print(") "+EPLConstants.OPERATOR_AND+" "+EPLConstants.OPERATOR_NOT+" "+EPLConstants.TIMER_INTERVAL+"("+notTime.getNot().getDuration()+") ");
									//System.out.print(") and not "+notTime.getNot().getName()+" "+notTime.getNot().getDuration()+" ");
									openParaStack.pop();
									if(openParaStack.size() > 0){
										stackTop = openParaStack.peek();
									}
									else{
										stackTop = null;
									}
								}
								else{
									throw new BDPLTranslateException("Time delays are overlapping");
								}
							}
						}
						
						notOpenPara = false;
						entry = notTable.getEntryByNotStart(term);
						if(entry != null){
							if(BDPLTranslateUtil.getTermType(entry.getNot()) == BDPLTranslateUtil.TERM_TIME){
								openParaStack.push(entry);
								stackTop = openParaStack.peek();
								notOpenPara = true;
							}
							else{
								// put only not event in list
								notList.put(entry.getNotEnd(), entry.getNot());
							}
						}
					}
					
					
					// term -> ) : time delay entries ending at end
					right = tdTable.getEntriesByEnd(null);
					BDPLTranslateUtil.sortTimeDelayEntryByStart(right, seqc);
						/*for(int i = 0; i < right.size(); i++){
							System.out.println("null end "+right.get(i).getStart().getName());
						}*/
					// term -> ( : time delay entries starting from the last term
					left = tdTable.getEntriesByStart(term);
					BDPLTranslateUtil.sortTimeDelayEntryByEnd(left, seqc);
						
						//System.out.println(term.getName()+" start "+left.size());
					if(left.size() <= 1){
						if(left.size() == 1){
							if(right.size() > 0 && right.get(right.size()-1) == left.get(0)){
								System.out.print(EPLConstants.OPERATOR_SEQ+" ( "+EPLConstants.TIMER_INTERVAL+"("+left.get(0).getDuration()+") ");
								//System.out.print("-> ( interval "+left.get(0).getDuration()+" ) ");
								right.remove(right.size()-1);
							}
						}
					}
					else{
						throw new BDPLTranslateException("Time delays are duplicated at end");
					}
					
					
					for(int i = 0; i < right.size(); i++){
						TimeDelayEntry temp = right.get(i);
						if(temp == stackTop){
							System.out.print(") "+EPLConstants.OPERATOR_AND+" "+EPLConstants.TIMER_INTERVAL+"("+temp.getDuration()+") ");	
							//System.out.print(") and interval "+temp.getDuration()+" ");
							openParaStack.pop();
							if(openParaStack.size() > 0){
								stackTop = openParaStack.peek();
							}
							else{
								stackTop = null;
							}
						}
						else{
							throw new BDPLTranslateException("Time delays are overlapping");
						}
					}
				}
			}
			// only connected time delay
			else{
				if(tdTable == null){
					throw new BDPLTranslateException("Null Expession");
				}
				else{
					TimeDelayEntry temp = tdTable.getEntries().get(0);
					System.out.print(EPLConstants.TIMER_INTERVAL+"("+temp.getDuration()+") ");
					//System.out.print("interval "+temp.getDuration()+" ");
				}
			}
			
			System.out.print(") ");

		}
		
		
		
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
		
		
		private class SeqTerms{
			private int insertIndex;
			
			private final SeqClause sequence;
			
			public SeqTerms(int i, SeqClause seq){
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
