package com.hp.hpl.jena.sparql.syntax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class ElementEventBinOperator extends Element {

	String type;
	Element left;
	Element right;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ElementEventBinOperator(String type){
		this.type = type;
	}
	
	public String getTyp() {
		return type;
	}

	public void setTyp(String typ) {
		this.type = typ;
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

	public void setChilds(Element left, Element right) {
		this.left = left;
		this.right = right;
	}
	
	public Element getLeft() {
		return this.left;
	}

	public void setLeft(Element left) {
		this.left = left;
	}

	public Element getRight() {
		return this.right;
	}

	public void setRight(Element right) {
		this.right = right;
	}

}
