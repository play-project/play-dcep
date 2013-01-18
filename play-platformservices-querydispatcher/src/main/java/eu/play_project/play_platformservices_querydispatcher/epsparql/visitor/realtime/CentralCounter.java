package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;

import java.util.Stack;

public class CentralCounter {
	long ceid; //Complex event id variable.
	long triplestoreVariable;
	long absVariable;
	long filterVar;
	Stack<Long> filterVars;
	

	static CentralCounter counter;
	
	private CentralCounter(){
		ceid = 0;
		triplestoreVariable = 0;
		absVariable = 0;
		filterVars = new Stack();
	}
	
	public static CentralCounter getCentralCounter(){
		if(counter==null){
			counter = new CentralCounter();
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
