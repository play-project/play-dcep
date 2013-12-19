package eu.play_project.play_platformservices_querydispatcher.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.Query;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.general.VariableTypeVisitor;


/**
 * Contains type informations about variables.
 * @author sobermeier
 *
 */
public class VariableTypeManager {
	
	Map<String, Integer> variables;
	Query query;
	
	public VariableTypeManager(Query q){
		variables = new HashMap<String, Integer>();
		this.query = q;
	}
	
	public void collectVars(){
		VariableTypeVisitor vv = new VariableTypeVisitor(this);
		
		vv.collectVariables(query);
	}

	/**
	 * Add new variable to list or update existing variable.
	 */
	public void addVariable(String varName, int type){
		if(variables.containsKey(varName)){
			if(!isType(varName,type)){
				variables.put(varName, variables.get(varName) + type);
			}
		}else{
			variables.put(varName, type);
		}
	}

	
	/**
	 * Check if variable is of the given type.
	 * @param varName Varname.
	 * @return True if corresponding variable to given name is of given type. Else it is false.
	 */
	public boolean isType(String varName, int type){
		Integer varType = variables.get(varName);
		return !((varType &= (type)) == 0);
	}

	/**
	 * Get type of variable with name stored in varName.
	 */
	public Integer getType(String varName){
		return variables.get(varName);
	}
	
	/**
	 * Returns all variables with given type.
	 */
	public List<String> getVariables(int type){
		List<String> vars =  new LinkedList<String>();
		for (String var : variables.keySet()) {
			if(this.isType(var, type)){
				vars.add(var);
			}
		}
		return vars;
	}
	
	/**
	 * Returns all variables with given type1 and type2.
	 */
	public List<String> getIntersection(int type1, int type2) {
		List<String> vars =  new LinkedList<String>();
		for (String var : variables.keySet()) {
			if(this.isType(var, type1)){
				if(this.isType(var, type2)){
					vars.add(var);
				}
			}
		}
		return vars;
	}
	
	/**
	 * Returns all variables with are in historic part and in real time or construct part.
	 */
	public Set<String> getSelectSharedVariables(int realtime, int construct, int historic) {
		Set<String> vars =  new HashSet<String>();
		for (String var : variables.keySet()) {
			if(this.isType(var, historic)){
				if(this.isType(var, construct)){
					vars.add(var);
				}
				if(this.isType(var, realtime)){
					vars.add(var);
				} 
			}
		}
		return vars;
	}
	
}
