package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

/**
 * Count the number of events in a query.
 * @author sobermeier
 *
 */
public class CountEventsVisitor extends GenericVisitor {
	int numberOfEvents = 0;
	
	public void count(Element el) {
		numberOfEvents = 0;
		el.visit(this);
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		numberOfEvents++;
	}
	
	@Override
	public void visit(ElementEventBinOperator el) {
		el.getLeft().visit(this);
		el.getRight().visit(this);
	}

	public int getNumberOfEvents() {
		return this.numberOfEvents;
	}
}
