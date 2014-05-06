package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementBraceOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;

/**
 * Count the number of events in a query.
 * @author sobermeier
 *
 */
public class CountEventsVisitor extends GenericVisitor {
	int numberOfEvents = 0;
	
	public int count(Element el) {
		numberOfEvents = 0;
		el.visit(this);
		return numberOfEvents;
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		numberOfEvents++;
	}
	
	@Override
	public void visit(ElementBraceOperator el) {
		el.getSubElements().visit(this);
	}
	
	@Override
	public void visit(ElementNotOperator el) {
		el.getStart().visit(this);
		el.getEnd().visit(this);
		el.getNot().visit(this);
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
