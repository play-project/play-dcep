package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

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

// Simple implementation only one avg.
public class HavingVisitor extends GenericVisitor implements ExprVisitor {
	private StringBuffer code;
	private VarNameManager vm;

	public HavingVisitor() {
		code = new StringBuffer();
		vm = VarNameManager.getVarNameManager();
	}

	public StringBuffer getCode() {
		return code;
	}

	public void setCode(StringBuffer code) {
		this.code = code;
	}

	@Override
	public void visit(ExprFunction2 arg0) {
		arg0.getArg1().visit(this);
		vm.getNextResultVar2();
		arg0.getArg2().visit(this);

		if (arg0 instanceof E_GreaterThanOrEqual) {
			code.append(", greaterOrEqual(" + vm.getResultVar1() + ", "
					+ vm.getResultVar2() + ")");
		}
	}

	@Override
	public void visit(ExprAggregator arg0) {
		arg0.visit(this);
		vm.addAggregatVar(arg0.getAggregator().getExpr().getVarName());
		// For not nested expressions. E.g. AVG(?value)
		code.append("calcAverage(" + vm.getAggrDbId() + ", "
				+ vm.getWindowTime() + ", " + vm.getResultVar1() + ")");
		
		if (arg0.getAggregator() instanceof AggMax){
			
		}else if(arg0.getAggregator() instanceof AggMin){
			
		}else if(arg0.getAggregator() instanceof AggSum){
			
		}else if(arg0.getAggregator() instanceof AggCount){
			
		}else if(arg0.getAggregator() instanceof AggAvg){
			
		}else if(arg0.getAggregator() instanceof AggSample){
			
		}
	}


	public void visit1(ExprAggregator arg0) {
		vm.addAggregatVar(arg0.getAggregator().getExpr().getVarName());
		// For not nested expressions. E.g. AVG(?value)
		code.append("calcAverage(" + vm.getAggrDbId() + ", "
				+ vm.getWindowTime() + ", " + vm.getResultVar1() + ")");
	}

	@Override
	public void visit(NodeValue arg0) {
		AggMax arg1;
		vm.setResultVar2("'" + arg0.toString() + "'");
	}

	@Override
	public void visit(ExprFunction0 func) {
		
	}

	@Override
	public void visit(ExprFunction1 func) {
	}


	@Override
	public void visit(ExprFunctionN func) {

	}

	@Override
	public void visit(ExprFunctionOp funcOp) {

	}


	@Override
	public void visit(ExprVar nv) {

	}
}
