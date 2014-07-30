package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.E_Now;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementBraceOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;


public class FilterExpressionCodeGenerator extends GenereicFilterExprVisitor {
	Logger logger;
	StringBuffer ele;
	StringBuffer emptyStringBuffer;
	UniqueNameManager cC;
	Stack<String> stack;
	boolean getStringFirstTime = false;

	public FilterExpressionCodeGenerator() {
		logger = LoggerFactory.getLogger(FilterExpressionCodeGenerator.class);
		cC = UniqueNameManager.getVarNameManager();
		stack = new Stack<String>();
		ele = new StringBuffer();
		emptyStringBuffer = new StringBuffer();

	}
	/**
	 * Use this method to start visiting filter elements.
	 * @param el Element to visit.
	 */
	public void startVisit(Element el){
		getStringFirstTime = true;
		ele = new StringBuffer();
		ele.append(" (");
		if(el != null){
			el.visit(this);
		}
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		el.getFilterExp();
	}
	
	@Override
	public void visit(ElementNotOperator el) {
		el.getStart().visit(this);
		el.getEnd().visit(this);
	}
	
	@Override
	public void visit(ElementBraceOperator el) {
		el.getSubElements().visit(this);
	}
	
	@Override
	public void visit(ElementFilter el) {
		el.getExpr().visit(this);
	}

	@Override
	public void visit(ExprFunction2 func) {
		logger.debug("Visit1: {}", func.getClass().getName());
		// Transform infix operators to prefix operators. E.g. (1 + 2) -3 -> plus(1, 2, R), minus(R, 3, R2)
		// Use post-order traversal except if node is a infix operator in prolog. In this case use In-order traversal.

		//Put boolean operands in parenthesis. (left parenthesis)
		if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalAnd) {
			ele.append("("); // AND representation in prolog.
		}else if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalOr) {
			ele.append("(");  // OR representation in prolog
		}
		
		
		// Get left values
		func.getArg1().visit(this);
		
		//Infix operator
		if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalAnd) {
			ele.append(")), (("); // AND representation in prolog.
			stack.push("");
		}else if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalOr) {
			ele.append(")); ((");  // OR representation in prolog
			stack.push(""); 
		}
		
		// Right values
		func.getArg2().visit(this);
		
		//Put boolean operands in parenthesis. (right parenthesis)
		if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalAnd) {
			ele.append(")"); // AND representation in prolog.
		}else if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalOr) {
			ele.append(")");  // OR representation in prolog
		}
		

		String  rightElem = stack.pop();

		// Operator
		if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalAnd) {
			// Do nothing AND is infix operator
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_LogicalOr) {
			// Do nothing OR is infix operator
		}else if (func instanceof com.hp.hpl.jena.sparql.expr.E_LessThan) {
			if(ele.length()>2 && (!ele.toString().endsWith(", ") && !ele.toString().endsWith("("))) ele.append(",");
			ele.append("less(" + stack.pop() + ", " + rightElem + ")");
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Subtract) {
			ele.append("minus(" + stack.pop() + "," + rightElem + ", " + cC.getNextFilterVar() + ")");
			stack.push(cC.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Multiply) {
			ele.append("multiply(" + stack.pop() + "," + rightElem + ", " + cC.getNextFilterVar() + ")");
			stack.push(cC.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Divide) {
			ele.append("/");
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Add) {
			ele.append("plus(" + stack.pop() + "," + rightElem + ", " + cC.getNextFilterVar() + ")");
			stack.push(cC.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual) {
			
			ele.append("greaterOrEqual(" + stack.pop() + "," + rightElem + ")");
			stack.push(cC.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_GreaterThan) {
			if(ele.length()>2 && (!ele.toString().endsWith(", ") && !ele.toString().endsWith("("))) ele.append(","); // TODO look if this is needed for other operators
			ele.append("greater(" + stack.pop() + "," + rightElem + ")");
			stack.push(cC.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_Equals) {
			ele.append("equal(" + stack.pop() + "," + rightElem +")");
			stack.push(cC.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_NotEquals) {
			ele.append("notEqual(" + stack.pop() + "," + rightElem +")");
			stack.push(cC.getFilterVar());
		} else if (func instanceof com.hp.hpl.jena.sparql.expr.E_StrContains) {
			ele.append( " (xpath(element(sparqlFilter, [keyWord="
					+ stack.pop()
					+ "], []), //sparqlFilter(contains(@keyWord,'");
					//Cutting away opening and closing quotes.
					if(rightElem.startsWith("(")) {
						ele.append(rightElem.substring(1, (rightElem.length()-1)) + "')), _))");
					} else {
						ele.append(rightElem + "')), _))");
					}
			stack.push(cC.getFilterVar());
		} else {
			throw new RuntimeException("Operator not implemented " + func.getClass().getName());
		}
	}

	@Override
	public void visit(ExprFunction1 func) {
	
		func.getArg().visit(this);

		if (func instanceof com.hp.hpl.jena.sparql.expr.E_NumAbs) {
			logger.debug(" Visit1: {}", func.getClass().getName());
			ele.append(", abs(");
			ele.append(stack.pop() + ", " + stack.push(cC.getNextFilterVar()) + ")");

		} else {
			throw new RuntimeException("Operator not implemented" + func.getClass().getName());
		}

	}

	@Override
	public void visit(ExprVar nv) {
		stack.push("V" + nv.getVarName());
	}

	public StringBuffer getEle() {
		
		//Close bracket
		if(getStringFirstTime){
			ele.append(")");
			getStringFirstTime = false;
		}

		if(ele.toString().endsWith("()")){
			ele = emptyStringBuffer;
		}
		return ele;
	}

	@Override
	public void visit(NodeValueDecimal nv) {
		stack.push(nv.toString().replace("\"", ""));
	}

	@Override
	public void visit(NodeValue nv) {
		stack.push(nv.toString().replace("\"", ""));
	}
	
	@Override
	public void visit(ExprFunction0 func) {
		if(func instanceof E_Now){
			// TODO sobermeier implement it if needed.
			// Use: parse_time('2009-09-19T23:55:00-04:00', R)...
			// get_time(F).
			System.out.println(func.getOpName());
		}else{
			logger.info("ExprFunction '{}' will be ignored. No ELE code will be generated for this token. {}", func.getClass().getName(), this.getClass().getSimpleName());
		}
	}
}
