package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;

import java.util.HashMap;
import java.util.Map;
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
	long resultVar1;
	String resultVar1s;
	long resultVar2;
	String resultVar2s;
	String windowTime;
	Map<String, Boolean> aggrVars;
	
	Stack<Long> filterVars;
	

	static VarNameManager counter;
	
	private VarNameManager(){
		ceid = 0;
		triplestoreVariable = 0;
		absVariable = 0;
		aggrDbId = 0;
		filterVars = new Stack();
		aggrVars = new HashMap<String, Boolean>();
		
	}
	
	public static VarNameManager getVarNameManager(){
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
	
	public boolean isAggregatVar(String varName){
		return aggrVars.containsKey(varName);
	}
	
	public void addAggregatVar(String varName){
		aggrVars.put(varName, new Boolean(true));
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
	
	public String  getNextFilterVar() {
		return "FilterVar" + ++filterVar;
	}
	
	public String getAggrDbId() {
		return "dbId"+ aggrDbId;
	}

	public String getNextAggrDbId() {
		return "dbId"+ aggrDbId++;
	}
	
	public String getWindowTime() {
		return windowTime;
	}

	public void setWindowTime(String windowTime) {
		this.windowTime = windowTime;
	}

	public String getResultVar1() {
		if(resultVar1s ==null){
			return "Result1" + resultVar1;
		}else{
			return resultVar1s;
		}
	}

	public String getNextResultVar1() {
		resultVar1s = null;
		return "Result1" + resultVar1++;
	}

	public String getResultVar2() {
		if(resultVar2s ==null){
			return "Result2" + resultVar2;
		}else{
			return resultVar2s;
		}
		
	}

	public String getNextResultVar2() {
		resultVar2s = null;
		return "Result2" + resultVar2++;
	}

	public void setResultVar1(String resultVar1) {
		this.resultVar1s = resultVar1;
	}

	public void setResultVar2(String resultVar2) {
		this.resultVar2s = resultVar2;
	}

	
}
