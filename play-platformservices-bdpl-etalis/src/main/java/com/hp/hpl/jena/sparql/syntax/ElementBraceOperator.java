package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class ElementBraceOperator extends ElementCep {

	Element subElements;
	
	public ElementBraceOperator(Element subElements) {
		this.subElements = subElements;
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
	
	public Element getSubElements() {
		return this.subElements;
	}

	public void setSubElements(Element subElements) {
		this.subElements = subElements;
	}

}
