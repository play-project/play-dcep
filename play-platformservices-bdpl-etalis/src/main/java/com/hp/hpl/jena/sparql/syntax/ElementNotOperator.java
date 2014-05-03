package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/**
 * Represents a BDPL not operator.
 * 
 * NOT(StartEventClause, NotAppearingEventClause, EndCondition)
 * 
 * @author sobermeier
 *
 */
public class ElementNotOperator extends ElementCep { // extends ElementNamedGraph{ // extends Element
	
	private Element start;
	private Element not;
	private Element end;
	
	public ElementNotOperator(Element start, Element not, Element end) {
		this.start =  start;
		this.not = not;
		this.end = end;
	}
	
	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
		return false;
	}
	
	public Element getStart() {
		return this.start;
	}

	public Element getNot() {
		return this.not;
	}

	public Element getEnd() {
		return this.end;
	}
}
