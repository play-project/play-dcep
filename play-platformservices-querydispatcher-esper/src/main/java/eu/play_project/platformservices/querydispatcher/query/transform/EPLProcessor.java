/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.parser.sparql.ast.ASTA;
import org.openrdf.query.parser.sparql.ast.ASTB;
import org.openrdf.query.parser.sparql.ast.ASTC;
import org.openrdf.query.parser.sparql.ast.ASTEventGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTEventPattern;
import org.openrdf.query.parser.sparql.ast.ASTNotClause;
import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;
import org.openrdf.query.parser.sparql.ast.ASTTimeBasedEvent;
import org.openrdf.query.parser.sparql.ast.Node;
import org.openrdf.query.parser.sparql.ast.VisitorException;
import eu.play_project.platformservices.querydispatcher.query.transform.util.AndClause;
import eu.play_project.platformservices.querydispatcher.query.transform.util.BDPLTransformException;
import eu.play_project.platformservices.querydispatcher.query.transform.util.OrClause;
import eu.play_project.platformservices.querydispatcher.query.transform.util.SeqClause;
import eu.play_project.platformservices.querydispatcher.query.transform.util.Term;




import org.openrdf.query.MalformedQueryException;

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
		
		try {
			qc.jjtAccept(translator, null);
		} catch (VisitorException e) {
			e.printStackTrace();
		}
	}
	
	private static class EPLTranslator extends eu.play_project.platformservices.bdpl.parser.ASTVisitorBase {
		
		@Override
		public Object visit(ASTEventPattern node, Object data)
			throws VisitorException
		{
			/*if(node.getTop()){
				System.out.print("\ntree: pattern[ ");
				super.visit(node, data);
				System.out.print(" ]\n");
			}
			else{
				System.out.print(" ( ");
				super.visit(node, data);
				System.out.print(" ) ");
			}
			
			return data;
			*/
			
			OrClause ret;
			
			// EventPattern = C
			if(node.jjtGetNumChildren() <= 1){
				ret = (OrClause) super.visit(node, data);
				
				//test output
				if(node.getTop())
				{
					printExpression(ret);
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
				} catch (BDPLTransformException e) {
					throw new VisitorException(e.getMessage());
				}
				
				//test output
				if(node.getTop())
				{
					printExpression(ret);
				}
				
				return ret;
			}
			
		}
		
		
		@Override
		public Object visit(ASTC node, Object data)
				throws VisitorException
		{
			/*System.out.print(" "+node.getOperator()+" ");
			super.visit(node, data);
			
			return data;*/
			
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
			/*System.out.print(" "+node.getOperator()+" ");
			super.visit(node, data);
			
			return data;*/
			
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
				} catch (BDPLTransformException e) {
					throw new VisitorException(e.getMessage());
				}
			}
		}
		
		@Override
		public Object visit(ASTA node, Object data)
				throws VisitorException
		{
			/*System.out.print(" "+node.getOperator()+" ");
			super.visit(node, data);
			
			return data;*/
			
			return super.visit(node, data);
		}
		
		@Override
		public Object visit(ASTNotClause node, Object data)
				throws VisitorException
		{
			/*System.out.print(" NOT: ");
			
			super.visit(node, data);
			return data;*/
			
			super.visit(node, data);
			
			Term term = new Term("not", 1);
			SeqClause seq = new SeqClause();
			seq.addTerm(term);
			AndClause and = new AndClause();
			and.addSeqClause(seq);
			OrClause expression = new OrClause();
			expression.addAndClause(and);
			
			return expression;
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
			
			Term term = new Term("time", 1);
			SeqClause seq = new SeqClause();
			seq.addTerm(term);
			AndClause and = new AndClause();
			and.addSeqClause(seq);
			OrClause expression = new OrClause();
			expression.addAndClause(and);
			
			return expression;
		}
		
		@Override
		public Object visit(ASTEventGraphPattern node, Object data)
				throws VisitorException
		{
			/*System.out.print(" "+node.getOperator()+" ");
			System.out.print(" "+node.getEventName()+" ");
			
			super.visit(node, data);
			
			return data;*/
			
			super.visit(node, data);
			
			Term term = new Term(node.getEventName(), 1);
			SeqClause seq = new SeqClause();
			seq.addTerm(term);
			AndClause and = new AndClause();
			and.addSeqClause(seq);
			OrClause expression = new OrClause();
			expression.addAndClause(and);
			
			return expression;
		}
		
		/*
		 * Exp = Exp (or Exp)+
		 */
		private OrClause unionOrExpression(List<OrClause> expressions){
			OrClause ret = expressions.get(0);
			
			for(int i = 1; i < expressions.size(); i++){
				OrClause orts = expressions.get(i);
				List<AndClause> arts = orts.getAndClauses();
				
				for(int j = 0; j < arts.size(); j++){
					ret.addAndClause(arts.get(j));
				}
			}
			
			return ret;
		}
		
		/*
		 * Exp = ((TERM (seq TERM)*:=SEQ CLAUSE) (and SEQ CLAUSE)*:= AND CLAUSE) (or AND CLAUSE)*:=OR CLAUSE)
		 */
		
		
		/*
		 * Exp = Exp (seq Exp)+
		 * 
		 * @param expressions 
		 * @return united expression
		 */
		private OrClause unionSeqExpression(List<OrClause> expressions) throws BDPLTransformException{
			
			OrClause ret = new OrClause();
			combineAndClause(new ArrayList<AndClause>(), expressions, 1, ret);
			
			return ret;
		}
		
		/*
		 * Exp = Exp (and Exp)+
		 */
		private OrClause unionAndExpression(List<OrClause> expansions) throws BDPLTransformException{
			OrClause ret = new OrClause();
			combineAndClause(new ArrayList<AndClause>(), expansions, 2, ret);
			
			return ret;
		}
		
		/*
		 * (AND CLAUSE (op AND CLAUSE)*):=combi (or (AND CLAUSE (op AND CLAUSE)*))* = ( AND CLAUSE (or AND CLAUSE)*):=exp (op (AND CLAUSE (or AND CLAUSE)*))*
		 * 
		 * @param combi a combination of and terms from each exps
		 * @param exps 
		 */
		private void combineAndClause(List<AndClause> combi, List<OrClause> exps, int type, OrClause result) throws BDPLTransformException{
			int size = exps.size();
			
			if(size > 0){
				// a stack of current AND CLAUSE of each expression
				int [] stack = new int [size];
				// a pointer to current expression from which a AND CLAUSE will be chosen
				int pointer = 0;
				stack[0] = 0;
				
				while(true){
					
					if(pointer < 0){
						break;
					}
					// an AND CLAUSE from the last expression is chosen, one combination is made
					// pointer goes back to the previous expression
					else if(pointer >= size){
						switch(type){
							case 1:{
								// (AND CLAUSE (seq AND CLAUSE)*) (or (AND CLAUSE (seq AND CLAUSE)*)) = (AND CLAUSE (or AND CLAUSE)*)(seq (AND CLAUSE (or AND CLAUSE)*))*
								expand1(combi, result);
								break;
							}
							case 2:{
								// AND CLAUSE (or AND CLAUSE)* = (AND CLAUSE (or AND CLAUSE)*)(and (AND CLAUSE (or AND CLAUSE)*))*
								expand2(combi, result);
								break;
							}
							case 3:{
								// (AND CLAUSE (seq AND CLAUSE)*) (or (AND CLAUSE (seq AND CLAUSE)*)) = (SEQ CLAUSE (or SEQ CLAUSE)*)(seq (SEQ CLAUSE (or SEQ CLAUSE)*))*
								expand3(combi, result);
								break;
							}
						}
						
						// remove the last AND CLAUSE
						combi.remove(combi.size()-1);
						pointer --;
					}
					// a further AND CLAUSE will be chosen here
					else{
						int index = stack[pointer];
						List<AndClause> ands = exps.get(pointer).getAndClauses();
						
						// chose a AND CLAUSE from this expression
						// pointer goes to the next expression
						if(index < ands.size()){
							combi.add(ands.get(index));
							stack[pointer] = index + 1;
							pointer++;
							
						}
						// all AND CLAUSE from this expression are chosen
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
		private void combineAndClauseRecursive(List<AndClause> combi, List<OrClause> exps, int depth, int type, OrClause result) throws BDPLTransformException{
			
			if(exps.size() > 0){
				// make combination of and terms from every expansion
				if(depth < exps.size()){
					OrClause otrs = exps.get(depth);
					
					for(int i = 0; i < otrs.getAndClauses().size(); i++){
						AndClause atrs = otrs.getAndClauses().get(i);
						// add the next and terms in this level
						combi.add(atrs);
						// go to the next depth
						combineAndClauseRecursive(combi, exps, depth+1, type, result);
						// remove the old and terms in this level
						combi.remove(combi.size()-1);
					}
				}
				else{
					switch(type){
						case 1:{
							// (AND CLAUSE (seq AND CLAUSE)*) (or (AND CLAUSE (seq AND CLAUSE)*)) = (AND CLAUSE (or AND CLAUSE)*)(seq (AND CLAUSE (or AND CLAUSE)*))*
							expand1(combi, result);
							break;
						}
						case 2:{
							// AND CLAUSE (or AND CLAUSE)* = (AND CLAUSE (or AND CLAUSE)*)(and (AND CLAUSE (or AND CLAUSE)*))*
							expand2(combi, result);
							break;
						}
						case 3:{
							// (AND CLAUSE (seq AND CLAUSE)*) (or (AND CLAUSE (seq AND CLAUSE)*)) = (SEQ CLAUSE (or SEQ CLAUSE)*)(seq (SEQ CLAUSE (or SEQ CLAUSE)*))*
							expand3(combi, result);
							break;
						}
					}
				}
			}
		}
		
		/*
		 * SEQ CLAUSE = (SEQ CLAUSE (seq SEQ CLAUSE)*):= combi
		 * 
		 * @param combi only used for passing data
		 */
		private void expand3(List<AndClause> combi, OrClause result){
			AndClause and = new AndClause();
			SeqClause seq = new SeqClause();
			List<Term> trs = seq.getTerms();
			and.addSeqClause(seq);
			
			for(int i = 0; i < combi.size(); i++){
				AndClause and1 = combi.get(i);
				List<Term> trs1 = and1.getSeqClauses().get(0).getTerms();
				
				for(int j = 0; j < trs1.size(); j++){
					trs.add(trs1.get(j));
				}
			}
			
			// write one OR CLAUSE in result
			result.addAndClause(and);
		}
		
		/*
		 *  Exp = (AND CLAUSE (and AND CLAUSE)*):= combi
		 */
		private void expand2(List<AndClause> combi, OrClause result) throws BDPLTransformException{
			
			OrClause or;
			// statistic data for expanding AND CLAUSE
			List<Term> singleTerms = new ArrayList<Term>();
			List<Term> timeTerms = new ArrayList<Term>();
			List<SeqClause> seqTerms = new ArrayList<SeqClause>();
			
			// every AND CLAUSE in this combination
			for(int i = 0; i < combi.size(); i++){
				AndClause and = combi.get(i);
				List<SeqClause> ls = and.getSeqClauses();
				
				prepareExpandAndClause(ls, singleTerms, timeTerms, seqTerms);
	
			}
			
			// (SEQ CLAUSE (or SEQ CLAUSE)*) = AND CLAUSE
			or = expandAndClause(singleTerms, timeTerms, seqTerms);
			List<AndClause> ands = or.getAndClauses();
			for(int j = 0; j < ands.size(); j++){
				result.addAndClause(ands.get(j));
			}	
			
			//test output
			
				/*AndClause first = combi.get(0);
				System.out.print("\n( ");
				List<SeqClause> lstrs = first.getSeqClauses();
				
				System.out.print("( ");
				SeqClause strs = lstrs.get(0);
				List<Term> ltrs = strs.getTerms();
				Term term = ltrs.get(0);
				System.out.print(term.getType()+" ");
				for(int j = 1; j < ltrs.size(); j++){
					term = ltrs.get(j);
					System.out.print("->"+term.getType()+" ");
				}
				System.out.print(") ");
			
				for(int i = 1; i < lstrs.size(); i++){
					System.out.print("and ( ");
					strs = lstrs.get(i);
					ltrs = strs.getTerms();
					
					term = ltrs.get(0);
						System.out.print(term.getType()+" ");
					for(int j = 1; j < ltrs.size(); j++){
						term = ltrs.get(j);
						System.out.print("->"+term.getType()+" ");
					}
					System.out.print(") ");
				}
				System.out.print(") ");
				
				for(int i = 1; i < combi.size(); i++){
					System.out.print("and ( ");
					first = combi.get(i);
					lstrs = first.getSeqClauses();
					
					System.out.print("( ");
					strs = lstrs.get(0);
					ltrs = strs.getTerms();
					term = ltrs.get(0);
					System.out.print(term.getType()+" ");
					for(int l = 1; l < ltrs.size(); l++){
						term = ltrs.get(l);
						System.out.print("->"+term.getType()+" ");
					}
					System.out.print(") ");
					
					for(int k = 1; k < lstrs.size(); k++){
						System.out.print("and ( ");
						strs = lstrs.get(k);
						ltrs = strs.getTerms();
						
						term = ltrs.get(0);
							System.out.print(term.getType()+" ");
						for(int j = 1; j < ltrs.size(); j++){
							term = ltrs.get(j);
							System.out.print("->"+term.getType()+" ");
						}
						System.out.print(") ");
					}
					System.out.print(") ");
				}*/
			
		}
		
		/*
		 *  Exp = (AND CLAUSE (seq AND CLAUSE)*):=combi
		 *  
		 *  @param combi only used for passing data
		 */
		private void expand1(List<AndClause> combi, OrClause result) throws BDPLTransformException{
			
			
			List<OrClause> seqs = new ArrayList<OrClause>();
			
			// statistic data for expanding AND CLAUSE
			List<Term> singleTerms = new ArrayList<Term>();
			List<Term> timeTerms = new ArrayList<Term>();
			List<SeqClause> seqTerms = new ArrayList<SeqClause>();
			
			// every AND CLAUSE in this combination
			for(int i = 0; i < combi.size(); i++){
				AndClause and = combi.get(i);
				List<SeqClause> ls = and.getSeqClauses();
				
				
				prepareExpandAndClause(ls, singleTerms, timeTerms, seqTerms);
				
				// (SEQ CLAUSE (or SEQ CLAUSE)*) = AND CLAUSE
				seqs.add(expandAndClause(singleTerms, timeTerms, seqTerms));
				
				
				singleTerms.clear();
				timeTerms.clear();
				seqTerms.clear();
			}
			
			// (SEQ CLAUSE (seq SEQ CLAUSE)*) (or SEQ CLAUSE (seq SEQ CLAUSE)*)* = (SEQ CLAUSE (or SEQ CLAUSE)*) (seq (SEQ CLAUSE (or SEQ CLAUSE)*))*
			combineAndClause(new ArrayList<AndClause>(), seqs, 3, result);
			
			
			/*AndTerms first = combi.get(0);
			System.out.print("\n( ");
			List<SeqTerms> lstrs = first.getTerms();
			
			System.out.print("( ");
			SeqTerms strs = lstrs.get(0);
			List<Term> ltrs = strs.getTerms();
			Term term = ltrs.get(0);
			System.out.print(term.getType()+" ");
			for(int j = 1; j < ltrs.size(); j++){
				term = ltrs.get(j);
				System.out.print("->"+term.getType()+" ");
			}
			System.out.print(") ");
		
			for(int i = 1; i < lstrs.size(); i++){
				System.out.print("and ( ");
				strs = lstrs.get(i);
				ltrs = strs.getTerms();
				
				term = ltrs.get(0);
					System.out.print(term.getType()+" ");
				for(int j = 1; j < ltrs.size(); j++){
					term = ltrs.get(j);
					System.out.print("->"+term.getType()+" ");
				}
				System.out.print(") ");
			}
			System.out.print(") ");
			
			for(int i = 1; i < combi.size(); i++){
				System.out.print("-> ( ");
				first = combi.get(i);
				lstrs = first.getTerms();
				
				System.out.print("( ");
				strs = lstrs.get(0);
				ltrs = strs.getTerms();
				term = ltrs.get(0);
				System.out.print(term.getType()+" ");
				for(int l = 1; l < ltrs.size(); l++){
					term = ltrs.get(l);
					System.out.print("->"+term.getType()+" ");
				}
				System.out.print(") ");
				
				for(int k = 1; k < lstrs.size(); k++){
					System.out.print("and ( ");
					strs = lstrs.get(k);
					ltrs = strs.getTerms();
					
					term = ltrs.get(0);
						System.out.print(term.getType()+" ");
					for(int j = 1; j < ltrs.size(); j++){
						term = ltrs.get(j);
						System.out.print("->"+term.getType()+" ");
					}
					System.out.print(") ");
				}
				System.out.print(") ");
			}*/
		}
		
		
		/*
		 * Gather statics information in AND CLAUSE
		 */
		private void prepareExpandAndClause(List<SeqClause> ls, List<Term> singleTerms, List<Term> timeTerms, List<SeqClause> seqTerms) throws BDPLTransformException{
			// every SEQ CLAUSE in this AND CLAUSE 
			Term maxTime = null;
			
			for(int j = 0; j < ls.size(); j++){
				SeqClause st = ls.get(j);
				
				// sequence terms
				if(st.getSize() > 1){
					seqTerms.add(st);
				}
				else{
					Term t = st.getTerms().get(0);
					
					//TODO change contain
					// event term
					if(t.getType().contains("event")){
						singleTerms.add(t);
						
						if(t.getDuration() != null){
							if(Integer.valueOf(t.getDuration()) > Integer.valueOf(maxTime.getDuration())){
								maxTime = t;
							}
						}
					}
					// time term
					else if(t.getType().contains("time")){
						// chose the maximal time
						if(maxTime != null){
							if(Integer.valueOf(t.getDuration()) > Integer.valueOf(maxTime.getDuration())){
								maxTime = t;
							}
						}
					}
					else{
						throw new BDPLTransformException("Invalid term type");
					}
				}
			}
			
			if(maxTime != null){
				timeTerms.add(maxTime);
			}
		}
		
		/*
		 * (SEQ CLAUSE (or SEQ CLAUSE)*) = ( AND CLAUSE )
		 * 
		 * 
		 * @param singleTerms only used for passing data
		 * @param timeTerms only used for passing data
		 * @param seqTerms only used for passing data
		 */
		private OrClause expandAndClause(List<Term> singleTerms, List<Term> timeTerms, List<SeqClause> seqTerms){
			
			OrClause ret = new OrClause();
			// the sequence into which new terms are inserted
			List<SeqTerms> seqs = new ArrayList<SeqTerms>();
			
			// insert single terms in AND CLAUSE
			int singleSize = singleTerms.size();
			//TODO check n should not be too big
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
				ret.addAndClause(atrs);
			}
			
			//test output
			
			/*List<AndTerms> lat = ret.getTerms();
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
			System.out.println("\nsize: "+lat.size());*/
			
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
		}
		
		/*
		 * Insert the terms in iseq into the seq.
		 * 
		 * @param seq the original sequence into which iseq is inserted
		 * @param iseq the sequence to be inserted
		 */
		private List<SeqTerms> insertSeq(List<SeqTerms> seqs, SeqClause iseq){
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
		 * Insert the term into the seq starting from the index.
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
		
		private void printExpression(OrClause result){
			List<AndClause> la = result.getAndClauses();
			if(la.size() > 0){
				AndClause first = la.get(0);
				System.out.print("\n( ");
				List<SeqClause> lstrs = first.getSeqClauses();
				
				System.out.print("( ");
				SeqClause strs = lstrs.get(0);
				List<Term> ltrs = strs.getTerms();
				Term term = ltrs.get(0);
				System.out.print(term.getType()+" ");
				for(int j = 1; j < ltrs.size(); j++){
					term = ltrs.get(j);
					System.out.print("-> "+term.getType()+" ");
				}
				System.out.print(") ");
			
				for(int i = 1; i < lstrs.size(); i++){
					System.out.print("and ( ");
					strs = lstrs.get(i);
					ltrs = strs.getTerms();
					
					term = ltrs.get(0);
						System.out.print(term.getType()+" ");
					for(int j = 1; j < ltrs.size(); j++){
						term = ltrs.get(j);
						System.out.print("-> "+term.getType()+" ");
					}
					System.out.print(") ");
				}
				System.out.print(") ");
				
				for(int i = 1; i < la.size(); i++){
					System.out.print("or ( ");
					first = la.get(i);
					lstrs = first.getSeqClauses();
					
					System.out.print("( ");
					strs = lstrs.get(0);
					ltrs = strs.getTerms();
					term = ltrs.get(0);
					System.out.print(term.getType()+" ");
					for(int l = 1; l < ltrs.size(); l++){
						term = ltrs.get(l);
						System.out.print("-> "+term.getType()+" ");
					}
					System.out.print(") ");
					
					for(int k = 1; k < lstrs.size(); k++){
						System.out.print("and ( ");
						strs = lstrs.get(k);
						ltrs = strs.getTerms();
						
						term = ltrs.get(0);
							System.out.print(term.getType()+" ");
						for(int j = 1; j < ltrs.size(); j++){
							term = ltrs.get(j);
							System.out.print("-> "+term.getType()+" ");
						}
						System.out.print(") ");
					}
					System.out.print(") ");
				}
				
			}
		}
		
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
