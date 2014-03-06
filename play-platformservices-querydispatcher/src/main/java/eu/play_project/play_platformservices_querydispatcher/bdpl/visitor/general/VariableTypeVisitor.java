package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.general;

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
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunction3;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.AggAvg;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.expr.aggregate.AggMax;
import com.hp.hpl.jena.sparql.expr.aggregate.AggMin;
import com.hp.hpl.jena.sparql.expr.aggregate.AggSample;
import com.hp.hpl.jena.sparql.expr.aggregate.AggSum;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
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
public class VariableTypeVisitor extends GenericVisitor{ // extends GenericVisitor {
	VariableTypeManager vm;
	int state; // Represents the part which is currently visited.

	public VariableTypeVisitor(VariableTypeManager vm){
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
		query.getEventQuery().visit(this);

		
		//Variables in historic part.
		state = VariableTypes.HISTORIC_TYPE;
		if(query.getQueryPattern()!=null){
			query.getQueryPattern().visit(this);
		}
		
		//Variables in having exp.
		for (Expr expr : query.getHavingExprs()) {
			expr.visit(this);
		}

	}
	
	@Override
	public void visit(ElementEventBinOperator el) {
		el.getLeft().visit(this);
		el.getRight().visit(this);
	}

	// Set type.
	@Override
	public Object visitVariable(Node_Variable it, String name) {
		vm.addVariable(name, state);
		return it;
	}
	
	@Override
	public void visit(ExprVar nv) {
		vm.addVariable(nv.getVarName(), state);
	}

	// Differentiate between different aggregators.
	// FIXME Expr can be a more complex exp. Only var is implemented.
	@Override
	public void visit(ExprAggregator arg0) {
		
		if (arg0.getAggregator() instanceof AggMax){
			state = VariableTypes.MAX_TYPE;
			(arg0.getAggregator()).getExpr().visit(this);
		}else if(arg0.getAggregator() instanceof AggMin){
			state = VariableTypes.MIN_TYPE;
			(arg0.getAggregator()).getExpr().visit(this);
		}else if(arg0.getAggregator() instanceof AggSum){
			state = VariableTypes.SUM_TYPE;
			(arg0.getAggregator()).getExpr().visit(this);
		}else if(arg0.getAggregator() instanceof AggCount){
			state = VariableTypes.CONSTRUCT_TYPE;
			(arg0.getAggregator()).getExpr().visit(this);
		}else if(arg0.getAggregator() instanceof AggAvg){
			state = VariableTypes.AVG_TYPE;
			(arg0.getAggregator()).getExpr().visit(this);
		}else if(arg0.getAggregator() instanceof AggSample){
			state = VariableTypes.SAMPLE_TYPE;
			(arg0.getAggregator()).getExpr().visit(this);
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

	// Visit all elements and mark all variables.
	@Override
	public void startVisit() {
		// Do nothing.
	}

	@Override
	public void visit(ExprFunction0 func) {
		// Do nothing.		
	}

	@Override
	public void visit(ExprFunction1 func) {
		func.getArg().visit(this);		
	}

	@Override
	public void visit(ExprFunction2 func) {
		func.getArg1().visit(this);
		func.getArg2().visit(this);
	}

	@Override
	public void visit(ExprFunction3 func) {
		func.getArg1().visit(this);
		func.getArg2().visit(this);
		func.getArg3().visit(this);
	}

	@Override
	public void visit(ExprFunctionN func) {
		for (Expr element : func.getArgs()) {
			element.visit(this);
		}
		
	}

	@Override
	public void visit(ExprFunctionOp funcOp) {
		funcOp.visit(this);
	}

	@Override
	public void visit(NodeValue nv) {
		// Do nothing.
	}

	@Override
	public void finishVisit() {
		// Do nothing.		
	}
	

}
