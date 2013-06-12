package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.AggAvg;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.expr.aggregate.AggMax;
import com.hp.hpl.jena.sparql.expr.aggregate.AggMin;
import com.hp.hpl.jena.sparql.expr.aggregate.AggSample;
import com.hp.hpl.jena.sparql.expr.aggregate.AggSum;
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

public class MathExprFunctionXVisitor  extends GenericVisitor {
	Logger logger;
	Stack<String> stack;
	StringBuffer code;
	UniqueNameManager uniqueNameManager;
	
	public MathExprFunctionXVisitor(){
		logger = LoggerFactory.getLogger(MathExprFunctionXVisitor.class);
		uniqueNameManager = UniqueNameManager.getVarNameManager();
		stack = new Stack<String>();
		code = new StringBuffer();
	}
	
	/**
	 * Generate CEP-Engine code for expressions with two value.
	 * Transform infix operators to prefix operators. E.g. (1 + 2)-3 -> plus(1, 2, R), minus(R, 3, R2) 
	 */
	@Override
	public void visit(ExprFunction2 func) {
		
		// Use post-order traversal except if node is a infix operator in prolog. In this case use in-order traversal.

		// Get left values
		func.getArg1().visit(this);
		
		//Infix operator
		if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalAnd) {
			code.append("), ("); // AND representation in prolog.
			stack.push(""); //NOP
		}else if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalOr) {
			code.append("); (");  // OR representation in prolog
			stack.push(""); //NOP
		}
		
		// Right values
		func.getArg2().visit(this);

		String  rightElem = stack.pop();
		
		// Operator
		if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalAnd) {
			// Do nothing AND is infix operator
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_LessThan) {
			if(code.length()>2) code.append(","); // At the beginning of a string no ",". 
			code.append("less(" + stack.pop() + ", " + rightElem + ")");
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Subtract) {
			code.append("minus(" + stack.pop() + "," + rightElem + ", " + uniqueNameManager.getNextFilterVar() + ")");
			stack.push(uniqueNameManager.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Multiply) {
			code.append("multiply(" + stack.pop() + "," + rightElem + ", " + uniqueNameManager.getNextFilterVar() + ")");
			stack.push(uniqueNameManager.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Divide) {
			code.append("/");
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Add) {
			code.append("plus(" + stack.pop() + "," + rightElem + ", " + uniqueNameManager.getNextFilterVar() + ")");
			stack.push(uniqueNameManager.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual) {
			code.append("greaterOrEqual(" + stack.pop() + "," + rightElem + ")");
			stack.push(uniqueNameManager.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_GreaterThan) {
			if(code.length()>2 && !code.toString().endsWith(",")) code.append(","); // TODO look if this is needed for other operators
			code.append("greater(" + stack.pop() + "," + rightElem + ")");
			stack.push(uniqueNameManager.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Equals) {
			code.append("equal(" + stack.pop() + "," + rightElem +")");
			stack.push(uniqueNameManager.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_NotEquals) {
			code.append("notEqual(" + stack.pop() + "," + rightElem +")");
			stack.push(uniqueNameManager.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_StrContains) {
			code.append( " (xpath(element(sparqlFilter, [keyWord="
					+ stack.pop()   
					+ "], []), //sparqlFilter(contains(@keyWord,'"
					//Cutting away opening and closing quotes.
					+ rightElem.substring(1, (rightElem.length()-1)) + "')), _))");
			stack.push(uniqueNameManager.getFilterVar());
		} else {
			throw new RuntimeException("Operator not implemented " + func.getClass().getName());
		}
	}
	
	@Override
	public void visit(ExprVar nv) {
		stack.push("V" + nv.getVarName());
	}
	
	@Override
	public void visit(NodeValueDecimal nv) {
		stack.push(nv.toString());
	}

	@Override
	public void visit(NodeValue nv) {
		nv.visit(new MathExprNodeVisitor());
		//stack.push(nv.visit(visitor) + "");
	}

	
	@Override
	public void visit(ExprAggregator arg0) {

		
		// This expression contains possibly more expressions. E.g. (1 + 2 - t).
		// For this reason visit the element and make post-order traversal.
		
		code.append("calcAverage(" + uniqueNameManager.getAggrDbId() + ", "
				+ uniqueNameManager.getWindowTime() + ", " + uniqueNameManager.getResultVar1() + "), ");
		
		stack.push(uniqueNameManager.getResultVar1());
		
		if (arg0.getAggregator() instanceof AggMax){
			
		}else if(arg0.getAggregator() instanceof AggMin){
			
		}else if(arg0.getAggregator() instanceof AggSum){
			
		}else if(arg0.getAggregator() instanceof AggCount){
			
		}else if(arg0.getAggregator() instanceof AggAvg){
			//code.append("calcAverage(" + uniqueNameManager.g);
		}else if(arg0.getAggregator() instanceof AggSample){
			
		}
	}
	
	public void visit1(ExprAggregator arg0) {

		// For not nested expressions. E.g. AVG(?value)
		code.append("calcAverage(" + uniqueNameManager.getAggrDbId() + ", "
				+ uniqueNameManager.getWindowTime() + ", " + uniqueNameManager.getResultVar1() + ")");
	}

	public StringBuffer getCode() {
		return code;
	}
	
class MathExprNodeVisitor implements NodeValueVisitor{
	@Override
	public void visit(NodeValueBoolean nv) {
		throw new RuntimeException(nv.toString() + "is not a valid value in math expressions.");
	}

	@Override
	public void visit(NodeValueDouble nv) {
		stack.push(nv.getDouble() + "");
	}

	@Override
	public void visit(NodeValueFloat nv) {
		stack.push(nv.getFloat() + "");
	}

	@Override
	public void visit(NodeValueInteger nv) {
		stack.push(nv.getInteger() + "");
	}


	@Override
	public void visit(NodeValueString nv) {
		throw new RuntimeException(nv.toString() + "is not a valid value in math expressions.");
	}

	@Override
	public void visit(NodeValueDT nv) {
		throw new RuntimeException(nv.toString() + "is not a valid value in math expressions.");
	}

	@Override
	public void visit(NodeValueDuration nodeValueDuration) {
		stack.push(nodeValueDuration.getDuration() + "");
	}

	@Override
	public void visit(NodeValueDecimal nv) {
		stack.push(nv.getDecimal() + "");		
	}

	@Override
	public void visit(NodeValueNode nv) {
		throw new RuntimeException(nv.toString() + "unknown datatype.");
	}
}


}
