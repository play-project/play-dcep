package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class ElementFnAbsFilter extends Element{
	Expr exp;
	
	public ElementFnAbsFilter(Expr mExp){
		this.exp = mExp;
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
	
	public Expr getExp() {
		return exp;
	}
}
