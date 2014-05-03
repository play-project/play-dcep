package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class RelationalOperator extends ElementCep {

	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
		// TODO Auto-generated method stub
		return false;
	}

}
