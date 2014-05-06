package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

public class CollectVariablesInTriplesAndFilterVisitor extends GenericVisitor{
	
	private Set<String> vars; 

	@Override
	public void visit(ElementEventGraph el) {
		vars = new HashSet<String>();
		// Visit event id.
		el.getGraphNameNode().visitWith(this);
		
		// Visit triples
		el.getElement().visit(this);
	}

	@Override
	public void visit(ElementGroup elg) {
		// Visit all group elements
		for (Element el : elg.getElements()) {
			el.visit(this);
		}
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {

		if(!name.startsWith("?")){ //Ignore blank nodes
			vars.add("V" + name);
		}

		return it;
	}

	@Override
	public void visit(ElementPathBlock el) {
		for (TriplePath tp : el.getPattern().getList()) {
			tp.getSubject().visitWith(this);
			tp.getPredicate().visitWith(this);
			tp.getObject().visitWith(this);
		}
	}
	
	@Override
	public void visit(ElementFilter el) {
		el.getExpr().visit(this);
	}
	
	@Override
	public void visit(ElementNotOperator el) {
		el.getStart().visit(this);
		el.getEnd().visit(this);
	}
	
	
	@Override
	public void visit(ExprFunction2 func) {
		logger.debug("Visit1: {}", func.getClass().getName());
		
		// Get left values
		func.getArg1().visit(this);
		
		// Right values
		func.getArg2().visit(this);
	}

	@Override
	public void visit(ExprFunction1 func) {
		func.getArg().visit(this);
	}

	@Override
	public void visit(ExprVar nv) {
		vars.add("V" + nv.getVarName());
	}

	/**
	 * Return all variables used in this query.
	 */
	public  Set<String> getVariables(){
		return vars;
	}


}
