package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;

/**
 * Visit all part of a query and collect variables.
 * Variables will be labeld, depending where they were found
 * 
 * @author sobermeier
 * 
 */
public class VariableVisitor extends GenericVisitor {
	VariableTypeManager vm;
	int state; // Represents the part which is currently visited.

	
	public void collectVariables(Query query){
		
		
		// Variables in construct part.
		state = VariableTypes.CONSTRUCT_TYPE;
		for(Triple triple: query.getConstructTemplate().getTriples()){
			triple.getSubject().visitWith(this);
			triple.getPredicate().visitWith(this);
			triple.getObject().visitWith(this);
		}
		
		
		//Variables in real time part.
		state = VariableTypes.REALTIME_TYPE;
		for(Element element : query.getEventQuery()){
			element.visit(this);
		}
		
		//Variables in historic part.
		state = VariableTypes.HISTORIC_TYPE;
		if(query.getQueryPattern()!=null){
			query.getQueryPattern().visit(this);
		}
		
		//Variables in having exp.
		for (Expr expr : query.getHavingExprs()) {
			for (Var var : expr.getVarsMentioned()) {
				vm.addVariable(var.getName(), VariableTypes.MIN_TYPE);
			}
		}
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
			vm.addVariable(var.getName(), state);
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
