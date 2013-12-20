package com.hp.hpl.jena.sparql.syntax;


public class ElementXpathContains extends ElementEventFilter {
	private String[] data;
	
	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}
	
	public void setFilterExp(String variable, String compareString){
		data = new String[2];
		data[0] = variable;
		data[1] = compareString;
	}



}
