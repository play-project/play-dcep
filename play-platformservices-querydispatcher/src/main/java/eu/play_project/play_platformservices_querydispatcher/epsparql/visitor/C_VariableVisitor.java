package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor;

import com.hp.hpl.jena.graph.Node_Variable;

import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.GenericVisitor;

public class C_VariableVisitor extends GenericVisitor{
	
	@Override
	public Object visitVariable(Node_Variable it, String name) {
		System.out.println("Variable: " + it.getClass().getName());
		return name;
	}

}
