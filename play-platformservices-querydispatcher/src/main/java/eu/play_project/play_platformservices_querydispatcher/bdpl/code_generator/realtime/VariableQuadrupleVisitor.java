package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;
import eu.play_project.play_platformservices_querydispatcher.types.C_Quadruple;
import eu.play_project.play_platformservices_querydispatcher.types.H_Quadruple;
import eu.play_project.play_platformservices_querydispatcher.types.P_Quadruple;
import eu.play_project.play_platformservices_querydispatcher.types.R_Quadruple;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Visit all variables in a query and detect their type.
 * @author Stefan Obermeier
 *
 */
public class VariableQuadrupleVisitor extends GenericVisitor {
	Map<String, List<Quadruple>> variables;
	VariableTypes currentType;

	
	public Map<String, List<Quadruple>> getVariables(Query query){
		
		variables = new HashMap<String, List<Quadruple>>();

		//C-Type
			C_VariableVisitor cVisitor = new C_VariableVisitor();
			//Visit all elements and check if it is a variable.
			for(Triple triple: query.getConstructTemplate().getTriples()){
				addToVariablelist(triple.getSubject().visitWith(cVisitor), triple, VariableTypes.constructType);
				addToVariablelist(triple.getPredicate().visitWith(cVisitor), triple, VariableTypes.constructType);
				addToVariablelist(triple.getObject().visitWith(cVisitor), triple, VariableTypes.constructType);
			}

		//R-Type
			R_VariableVisitor rVisitor = new R_VariableVisitor();
			rVisitor.setVariables(variables);
			for(Element element : query.getEventQuery()){
				element.visit(rVisitor);
			}

		//H-Type
			H_VariableVisitor hVisitor = new H_VariableVisitor();
			hVisitor.setVariables(variables);
			
			if(query.getQueryPattern()!=null){
				query.getQueryPattern().visit(hVisitor);
			}
			
			return variables;
	}

	// Add value to resultSet if it is not null.
		private void addToVariablelist(Object var, Triple triple,  VariableTypes type){
			if(var!=null){
					if(variables.get(var)==null){
						variables.put((String) var, new ArrayList<Quadruple>());
					}
					List<Quadruple> value = variables.get(var);
					switch (type){
					case constructType: value.add(new C_Quadruple(Node.createURI("http://construct.play-project.eu/"), tripleWithoutBlankNode(triple)));
						break;
					case historicType: value.add(new H_Quadruple(Node.createURI("http://construct.play-project.eu/"), tripleWithoutBlankNode(triple)));
						break;
					case realtimeType: value.add(new R_Quadruple(Node.createURI("http://construct.play-project.eu/"), tripleWithoutBlankNode(triple)));
						break;
					case preloadType: value.add(new P_Quadruple(Node.createURI("http://construct.play-project.eu/"), tripleWithoutBlankNode(triple)));
					}
					
					variables.put((String) var, value);
				}
		}
		
	private Triple tripleWithoutBlankNode(Triple triple) {
		boolean r = false;
		Triple output;
		Node s = null;
		Node p = null;
		Node o = null;

		// Check if triple contains a blank node.
		if (triple.getSubject().isBlank())
			r = true;
		else {
			if (triple.getPredicate().isBlank())
				r = true; // Possible in Notation 3
			else {
				if (triple.getObject().isBlank())
					r = true; // Possible in Notation 3
			}
		}

		// If triple contains a blank node generate a new triple without a blank
		// node.
		if (r) {
			//Subject
			if (triple.getSubject().isBlank()) {
				s = Node.createURI("http://blank.example.com/" //TODO sobermeir bettern name?
						+ triple.getSubject().toString());
			} else {
				s = triple.getSubject();
			}
			
			//Predicate
			if (triple.getPredicate().isBlank()) {
				p = Node.createURI("http://blank.example.com/"
						+ triple.getPredicate().toString());
			} else {
				p = triple.getPredicate();
			}
			
			//Object
			if (triple.getObject().isBlank()) {
				o = Node.createURI("http://blank.example.com/"
						+ triple.getObject().toString());
			} else {
				o = triple.getObject();
			}

			output =  new Triple(s,p,o);
		}else{
			output = triple;
		}
		return output;
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		System.out.println("Variable: " + it.getClass().getName());
		boolean exists = false; // true if variable is in the list
		//if(variables.containsKey(name)){
			//List<Quadruple> value = variables.get(name);
			//for (Quadruple variable : value) {
				//if(variable.getName().equals(name)){ // Variable exists in the list.
				//	exists = true;
				//	if(!variable.getTypes().contains(currentType)){ //If type do not exists -> add it.
				//		variable.getTypes().add(currentType);
				//	}
		//		}
		//	}
		//	if(!exists){
			//	value.add(new Variable(name, currentType));
//			}
//		}else{
//			ArrayList<Variable> list = new ArrayList<Variable>();
//			Variable var = new Variable(name, currentType);
//			list.add(var);
//			//variables.put(name,list);
//		}
		return name;
	}

	@Override
	public void visit(ElementNamedGraph el) {
		el.getGraphNameNode().visitWith(this);
		el.getElement().visit(this);
		
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementPathBlock el) {
		for(TriplePath obj :el.getPattern()){
			obj.getSubject().visitWith(this);
			obj.getPredicate().visitWith(this);
			obj.getObject().visitWith(this);
		}
	}

}
