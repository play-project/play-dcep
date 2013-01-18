package eu.play_project.querydispatcher.epsparql.Test.helpers;

import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;

public interface FilterOperators {
	
	public void visit(E_LogicalAnd el);

}
