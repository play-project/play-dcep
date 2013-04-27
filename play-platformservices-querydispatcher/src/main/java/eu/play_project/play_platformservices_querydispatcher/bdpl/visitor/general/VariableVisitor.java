package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.general;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.AnonId;
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
 * Variables will be labeled, depending where they were found
 * 
 * @author sobermeier
 * 
 */
public class VariableVisitor extends GenericVisitor {
	VariableTypeManager vm;
	int state; // Represents the part which is currently visited.

	public VariableVisitor(VariableTypeManager vm){
		this.vm = vm;
	}
	
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
				//TODO visit all types.
				vm.addVariable(var.getName(), VariableTypes.AVG_TYPE);
			}
		}
	}

	// Set type.
	@Override
	public Object visitVariable(Node_Variable it, String name) {
		vm.addVariable(name, state);
		return it;
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

	// Do nothing for this types.
	@Override
	public Object visitAny(Node_ANY arg0) {
		return arg0;
	}

	@Override
	public Object visitBlank(Node_Blank arg0, AnonId id) {
		return arg0;
	}

	@Override
	public Object visitLiteral(Node_Literal arg0, LiteralLabel lit) {
		return arg0;
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		return it;
	}

}
