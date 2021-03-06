package eu.play_project.play_platformservices_querydispatcher.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.query.Query;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.general.VariableTypeVisitor;


/**
 * Contains type informations about variables.
 * @author sobermeier
 *
 */
public class VariableTypeManager {
	
	private Map<String, Integer> variables;
	private Query query;
	Logger logger; 
	
	public VariableTypeManager(Query q){
		logger = LoggerFactory.getLogger(this.getClass());
		variables = new HashMap<String, Integer>();
		
		if (q != null) {
			this.query = q;
			this.collectVars();
		} else {
			logger.warn("Parameter shuld not be null.");
		}
	}
	
	private void collectVars(){
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
