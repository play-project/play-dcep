package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.Stack;

import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
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
import com.hp.hpl.jena.sparql.expr.aggregate.AggAvg;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.expr.aggregate.AggMax;
import com.hp.hpl.jena.sparql.expr.aggregate.AggMin;
import com.hp.hpl.jena.sparql.expr.aggregate.AggSample;
import com.hp.hpl.jena.sparql.expr.aggregate.AggSum;

/**
 * Generate code for having constrains.
 * @author sobermeier
 *
 */
public class HavingVisitor extends GenericVisitor implements ExprVisitor {
	private StringBuffer code; // Generated code.
	private MathExprFunctionXVisitor mathFuncVisitor;

	public HavingVisitor() {
		code = new StringBuffer();
		mathFuncVisitor = new MathExprFunctionXVisitor();
	}

	/**
	 * Get generated code if something was produced.
	 * @return Generated code.
	 */
	public StringBuffer getCode() {
		return code;
	}

	/*
	 * The visitor will append his generated code to the given StringBuffer.
	 */
	public void setCode(StringBuffer code) {
		this.code = code;
	}
	
	// Use MathExprFunctionXVisitor because he already knows how to deal with such expressions.

	
	@Override
	public void visit(ExprFunction1 arg0) {
		mathFuncVisitor.visit(arg0);
		code = mathFuncVisitor.getCode();
	}
	
	@Override
	public void visit(ExprFunction2 arg0) {
		mathFuncVisitor.visit(arg0);
		code = mathFuncVisitor.getCode();
	}
	
	@Override
	public void visit(ExprFunction3 arg0) {
		mathFuncVisitor.visit(arg0);
		code = mathFuncVisitor.getCode();
	}
	

	@Override
	public void visit(ExprAggregator arg0) {
		mathFuncVisitor.visit(arg0);
		code = mathFuncVisitor.getCode();
	}


	public void visit1(ExprAggregator arg0) {
		mathFuncVisitor.visit(arg0);
		code = mathFuncVisitor.getCode();
	}
}
