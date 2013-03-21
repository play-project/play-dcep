package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;

import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunction3;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitor;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class AggregateFunctionsVisitor extends GenericVisitor{

	@Override
	public void visit(ExprFunction2 arg0) {
		System.out.println(("Visit " + arg0.getClass().getName()));
		arg0.getArg1().visit(this);
		arg0.getArg2().visit(this);
	}

	
	@Override
	public void visit(ExprAggregator arg0) {
		System.out.println(arg0.getAggregator().getExpr());
		System.out.println(("Visit " + arg0.getClass().getName()));
	}
	
}
