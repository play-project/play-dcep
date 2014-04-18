package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
/**
 * Represents a filter expression in BDPL
 * @author sobermeier
 *
 */
public class ElementEventFilter extends Element{

	private String[] data;
	Expr expression;
	


	ElementEventFilter exp1;
	ElementEventFilter exp2;
	
	public ElementEventFilter(){}
	public ElementEventFilter(Expr expression){
		this.expression = expression;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setFilterExp(String variable,String operator, String compareString){
		data = new String[2];
		data[0] = variable;
		data[1] = compareString;
	}
	
	public Expr getExpression() {
		return expression;
	}
	public void setExpression(Expr expression) {
		this.expression = expression;
	}
	
	public void setFilterElement(Expr el){
		expression = el;
	}
	
	public String[] getData(){
		return data;
	}


	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}

}
