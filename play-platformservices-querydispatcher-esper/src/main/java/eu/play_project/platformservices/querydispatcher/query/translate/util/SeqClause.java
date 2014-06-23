/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate.util;

import java.util.ArrayList;
import java.util.List;


/**
 * An SeqClause is an expression of many Term connected with operator "seq".
 * On a SeqClause a TimeDelayTable may be attached describing all time delays
 * between events.
 * 
 * SeqClause := Term ( seq Term )*
 * 
 * 
 * 
 * @author ningyuan
 *
 */
public class SeqClause {
	
	private List<Term> terms = new ArrayList<Term>();
	
	private TimeDelayTable tdTable;
	
	public TimeDelayTable getTdTable() {
		return tdTable;
	}

	public void setTdTable(TimeDelayTable tdTable) {
		this.tdTable = tdTable;
	}

	public int getSize(){
		return terms.size();
	}
	
	public List<Term> getTerms(){
		return terms;
	}
	
	public boolean addTerm(Term term){
		return terms.add(term);
	}
}
