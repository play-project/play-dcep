package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.LinkedList;
import java.util.List;

import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;
/**
 *Manage unique values for:
 * 		 complex events (CEID)
 * 		 triplestore variable (ViD)
 * 		 Aggregate function db IDs.
 * @author sobermei
 *
 */
public class UniqueNameManager {
	private static VariableTypeManager vtm;
	private long ceid; //Complex event id variable.
	private long triplestoreVariable; // Represents the current triplestore variable.
	private long triplestoreVariableStart; // Store first triplestore variable of the query.
	private long triplestoreVariableEnd; // Store the last triplestore variabe of the query.
	private long absVariable;
	private long filterVar;
	private long aggrDbId;
	private long resultVar1;
	private String resultVar1s;
	private long resultVar2;
	private String resultVar2s;
	private String windowTime;
	

	static UniqueNameManager uniqueNameManger;
	
	private UniqueNameManager(){
		ceid = 0;
		triplestoreVariable = 0;
		absVariable = 0;
		aggrDbId = 0;
	}
	
	public static UniqueNameManager getVarNameManager(){
		if(uniqueNameManger == null){
			uniqueNameManger = new UniqueNameManager();
		}
		return uniqueNameManger;
	}
	
	/**
	 * Sometimes it is necessary to know all triple store variables of one query.
	 * With this method the current state will be persisted.
	 * Now it is possible to retrieve all triple store variables from this point with getAllTripleStoreVariablesOfThisQuery() .
	 */

	public void newQuery(int numberOfEvents){
		triplestoreVariableStart = triplestoreVariableEnd;
		triplestoreVariable = triplestoreVariableStart;
		triplestoreVariableEnd = triplestoreVariableEnd + numberOfEvents;
	}
	
	/**
	 * Generate a list of all triplestore variables of the current query.
	 * All variables after calling newQuery() are in this list.
	 * @return Triplestor variables of current Query.
	 */
	public List<String> getAllTripleStoreVariablesOfThisQuery(){
		LinkedList<String> vars = new LinkedList<String>();
		for (long i = triplestoreVariableStart; i < triplestoreVariableEnd; i++) {
			vars.add("ViD" + (i + 1));
		}

		return vars;
	}
	

	public long processNextEvent(){
		if (++triplestoreVariable >  triplestoreVariableEnd) {
			if (triplestoreVariable == 1 && triplestoreVariableEnd == 0) {
				throw new RuntimeException("UniqueNameManager was not initialized with newQuery(int numberOfEvents))");
			} else {
				throw new RuntimeException("No event left. This query schould contains " + (triplestoreVariableEnd - triplestoreVariableStart));
			}
		}
		return triplestoreVariable;
	}
	
	public void resetTriplestoreVariable(){
		triplestoreVariable = triplestoreVariableStart;
	}
	
	public long getCurrentSimpleEventNumber() {
		return triplestoreVariable;
	}
	
	public String getTriplestoreVariable(){
		return getTriplestoreVariableForEventNr(triplestoreVariable);
	}
	
	private String getTriplestoreVariableForEventNr(long eventNumber){
		return "ViD" + eventNumber;
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

	public String getWindowTime() {
		return windowTime;
	}

	public void setWindowTime(String windowTime) {
		this.windowTime = windowTime;
	}
	
	public String getNextCeid(){
		ceid++;
		return "CEID" + ceid;
	}
	
	public String getCeid(){
		return "CEID" + ceid;
	}
	
	/**
	 * This event can be produced by the engine and is related to the current triple store.
	 * @return Event name for a virtual event.
	 */
	public String getVirtualEvent() {
		return "virtualEvent" + triplestoreVariable;
	}
	
	public UniqueNameManager reset() {
		uniqueNameManger = new UniqueNameManager();
		return uniqueNameManger;
	}
	
}
