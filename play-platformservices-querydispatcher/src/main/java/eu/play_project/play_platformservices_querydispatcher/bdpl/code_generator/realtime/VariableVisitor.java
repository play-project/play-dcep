package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.Variable;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;

/**
 * Get variables from query
 * 
 * @author sobermeier
 * 
 */
public class VariableVisitor extends GenericVisitor {
	Map<String, List<Variable>> variables;
	VariableTypes currentType;
	int intType =0;
	
	public Map<String, List<Variable>> getVariables(Query query, VariableTypes type) {
		variables = new HashMap<String, List<Variable>>();
		getVariables(query, type, variables);
		return variables;
	}
	
	public Map<String, List<Variable>> getVariables(Query query, VariableTypes type,Map<String, List<Variable>> destination){
		switch(type){
		case constructType:
			currentType = type;
			intType = 1;
			for(Triple triple: query.getConstructTemplate().getTriples()){
				triple.getSubject().visitWith(this);
				triple.getPredicate().visitWith(this);
				triple.getObject().visitWith(this);
			}
			break;
		case realtimeType:
			currentType = type;
			intType = 2;
			for(Element element : query.getEventQuery()){
				element.visit(this);
			}
			break;
		case historicType:
			currentType = type;
			intType = 4;
			if(query.getQueryPattern()!=null){
				query.getQueryPattern().visit(this);
			}
			
			break;
		default:
			throw new RuntimeException("Type of the subquery is not known");
			
				
	}
		return destination;
	}

	

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		boolean exists = false; // true if variable is in the list
		if(variables.containsKey(name)){
			List<Variable> value = variables.get(name);
			for (Variable variable : value) {
				if(variable.getName().equals(name)){ // Variable exists in the list.
					exists = true;
					if(!variable.getTypes().contains(currentType)){ //If type do not exists -> add it.
						variable.getTypes().add(currentType);
						variable.setType((variable.getType()+intType));
					}
				}
			}
			if(!exists){
				Variable v = new Variable(name, currentType);
				v.setType((v.getType()+intType));
				value.add(v);
			}
		}else{
			ArrayList<Variable> list = new ArrayList<Variable>();
			Variable var = new Variable(name, currentType);
			var.setType((var.getType()+intType));
			list.add(var);
			variables.put(name,list);
		}
		return null;
	}

	@Override
	public void visit(ElementNamedGraph el) {
		el.getGraphNameNode().visitWith(this);
		el.getElement().visit(this);
	
	}


	@Override
	public void visit(ElementPathBlock el) {
		for(TriplePath obj :el.getPattern()){
			obj.getSubject().visitWith(this);
			obj.getPredicate().visitWith(this);
			obj.getObject().visitWith(this);
		}
	}

	@Override
	public void visit(ElementFilter el) {
		
		for (Var var : el.getExpr().getVarsMentioned()) {
			boolean exists = false; // true if variable is in the list
			//Test if variable exists in list. Else add new list
			if(variables.containsKey(var.getName())){
				List<Variable> value = variables.get(var.getName());
				for (Variable variable : value) {
					if(variable.getName().equals(var.getName())){ // Variable exists in the list.
						exists = true;
						if(!variable.getTypes().contains(currentType)){ //If type do not exists -> add it.
							variable.getTypes().add(currentType);
							variable.setType((variable.getType()+intType));
						}
					}
				}
				if(!exists){
					Variable v = new Variable(var.getName(), currentType);
					v.setType((v.getType()+intType));
					value.add(v);
				}
			}else{
				ArrayList<Variable> list = new ArrayList<Variable>();
				Variable var2 = new Variable(var.getName(), currentType);
				var2.setType((var2.getType()+intType));
				list.add(var2);
				variables.put(var.getName(),list);
			}
		}

	}
	
	@Override
	public void visit(ElementGroup el) {
		for (Element element : el.getElements()) {
			element.visit(this);
		}
		

	}

	
	@Override
	public void visit(ElementEventGraph el) {
		el.getGraphNameNode().visitWith(this);
		el.getElement().visit(this);
	}




}
