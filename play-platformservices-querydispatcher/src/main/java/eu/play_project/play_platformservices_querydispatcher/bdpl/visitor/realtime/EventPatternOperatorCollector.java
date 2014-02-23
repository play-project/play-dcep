package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

/**
 * Separate event patterns from operators. For more intuitive code generation.
 * Brackets are concatenated to the corresponding operator.
 * @author sobermeier
 *
 */

public class EventPatternOperatorCollector extends GenericVisitor {
	private List<ElementEventGraph> eventPatterns;
	private List<String> operators;
	StringBuffer opTmp;
	
	/**
	 * Collect event patterns and operators from given tree.
	 * Operators are combined with opening and closing brackets.
	 * @param root Root node of syntax tree.
	 */
	public void collectValues(Element root) {
		eventPatterns = new LinkedList<ElementEventGraph>();
		operators = new LinkedList<String>();
		opTmp = new StringBuffer();
		
		root.visit(this);
	}

	@Override
	public void visit(ElementEventBinOperator el) {
		el.getLeft().visit(this);
		
		if(el.getRight() instanceof ElementEventGraph) {
			operators.add(el.getTyp());
			el.getRight().visit(this);
		} else {
			
			opTmp.append(el.getTyp());
			opTmp.append("(");
			operators.add(opTmp.toString());
			opTmp = new StringBuffer();
			el.getRight().visit(this);
			
			opTmp.append(")");
		}
		
		if(opTmp.length() != 0) {
			operators.add(opTmp.toString());
		}
		
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		eventPatterns.add(el);
	}

	public List<ElementEventGraph> getEventPatterns() {
		return this.eventPatterns;
	}

	public List<String> getOperators() {
		return this.operators;
	}
}
