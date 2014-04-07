/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.transform.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ningyuan
 *
 */
public class AndClause {

	private List<SeqClause> clauses = new ArrayList<SeqClause>();
	
	public List<SeqClause> getSeqClauses(){
		return clauses;
	}
	
	public boolean addSeqClause(SeqClause clause){
		return clauses.add(clause);
	}
}
