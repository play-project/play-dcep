package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;

import java.util.Stack;
/**
 *Manage unique values for:
 * 		 complex events (CEID)
 * 		 triplestore variable (ViD)
 * 		 Aggregate function db IDs.
 * @author sobermei
 *
 */
public class VarNameManager {
	long ceid; //Complex event id variable.
	long triplestoreVariable;
	long absVariable;
	long filterVar;
	long aggrDbId;
	Stack<Long> filterVars;
	

	static VarNameManager counter;
	
	private VarNameManager(){
		ceid = 0;
		triplestoreVariable = 0;
		absVariable = 0;
		aggrDbId = 0;
		filterVars = new Stack();
		
	}
	
	public static VarNameManager getCentralCounter(){
		if(counter==null){
			counter = new VarNameManager();
		}
		return counter;
	}
	
	public String getNextCeid(){
		ceid++;
		return "CEID" + ceid;
	}
	
	public String getCeid(){
		return "CEID" + ceid;
	}
	
	public String getNextTriplestoreVariable(){
		triplestoreVariable++;
		return "ViD" + triplestoreVariable;
	}
	
	public String getTriplestoreVariable(){
		return "ViD" + triplestoreVariable;
	}
	
	public String getNextAbsVariable(){
		absVariable++;
		return "AbsVar" + absVariable;
	}
	
	public String getAbsVariable(){
		return "AbsVar" + absVariable;
	}
	
	public String  getFilterVar() {
		return "FilterVar" + filterVar + "";
	}
	
	public String  nextFilterVar() {
		return "FilterVar" + ++filterVar;
	}
	
}
