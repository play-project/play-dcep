package com.hp.hpl.jena.sparql.syntax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class ElementEventBinOperator extends Element {

	String type;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ElementEventBinOperator(String type){
		this.type = type;
		logger.debug("New BinOperator " + type);
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

}
