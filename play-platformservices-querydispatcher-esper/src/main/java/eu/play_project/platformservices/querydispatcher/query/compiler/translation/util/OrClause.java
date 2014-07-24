/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;

import java.util.ArrayList;
import java.util.List;


/**
 * An OrClause is an expression of many SeqClause connected with operator "or".
 * 
 * OrClause := SeqClause ( or SeqClause )*
 * 
 * 
 * 
 * @author ningyuan
 *
 */
public class OrClause {
	
	private List<SeqClause> clauses = new ArrayList<SeqClause>();
	
	public List<SeqClause> getSeqClauses(){
		return clauses;
	}
	
	public boolean addSeqClause(SeqClause clause){
		return clauses.add(clause);
	}
}
