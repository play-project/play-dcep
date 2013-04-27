package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** Evaluate a query element based on source information in a named collection. */
public class ElementEventGraph extends Element { // extends ElementNamedGraph{ // extends Element
	
	private Node sourceNode;
	private Element element;
	
	private Element filtexExp; 
	
	
	// GRAPH * (not in SPARQL)
	public ElementEventGraph(Element el) {
		this(null, el);
	}

	// GRAPH <uri> or GRAPH ?var
	public ElementEventGraph(Node n, Element el) {
		sourceNode = n;
		element = el;
	}

	public Node getGraphNameNode() {
		return sourceNode;
	}

	public Element getElement() {
		return element;
	}

	
	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}
	
	public void setFilterExp(Element element){
		filtexExp = element;
	}
	
	public Element getFilterExp(){
		return filtexExp;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
		return false;
	}

}
