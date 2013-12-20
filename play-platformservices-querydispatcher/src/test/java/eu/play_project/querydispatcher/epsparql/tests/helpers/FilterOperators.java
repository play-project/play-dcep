package eu.play_project.querydispatcher.epsparql.tests.helpers;

import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;

public interface FilterOperators {
	
	public void visit(E_LogicalAnd el);

}
