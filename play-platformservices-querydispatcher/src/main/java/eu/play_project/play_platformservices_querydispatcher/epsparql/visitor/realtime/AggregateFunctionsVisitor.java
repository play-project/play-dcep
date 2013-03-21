package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;

import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDT;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDuration;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueFloat;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class AggregateFunctionsVisitor extends GenericVisitor implements NodeValueVisitor{
	private StringBuffer code;
	
	public AggregateFunctionsVisitor(){
		code = new StringBuffer();
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
		arg0.getArg2().visit(this);

		if (arg0 instanceof E_GreaterThanOrEqual) {
			code.append("greaterOrEqual(A,B)");
		}
	}

	@Override
	public void visit(ExprAggregator arg0) {
		//For not nested expressions. E.g. AVG(?value)
		
		code.append("calcAverage" + "id, window, Result");
		arg0.getAggregator().getExpr().visit(this);
		System.out.println(("Visit " + arg0.getClass().getName()));
	}

	@Override
	public void visit(ExprVar arg0) {
		code.append("V" + arg0.getVarName());
	}
	@Override
	public void visit(NodeValueBoolean nv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(NodeValueDecimal nv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(NodeValueDouble nv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(NodeValueFloat nv) {
		code.append(nv);
	}


	@Override
	public void visit(NodeValueInteger nv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(NodeValueNode nv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(NodeValueString nv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(NodeValueDT nv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(NodeValueDuration nodeValueDuration) {
		// TODO Auto-generated method stub
		
	}
	
}
