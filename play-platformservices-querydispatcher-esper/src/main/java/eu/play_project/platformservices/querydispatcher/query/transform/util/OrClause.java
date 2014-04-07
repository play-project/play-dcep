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
public class OrClause {
	
	private List<AndClause> clauses = new ArrayList<AndClause>();
	
	public List<AndClause> getAndClauses(){
		return clauses;
	}
	
	public boolean addAndClause(AndClause clause){
		return clauses.add(clause);
	}
}
