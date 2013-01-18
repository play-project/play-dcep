package eu.play_project.play_platformservices_querydispatcher.api;

import eu.play_project.play_platformservices_querydispatcher.VariableWithValues;

public interface QueryDispatechElementVisitor {
	
	public void visit(VariableWithValues variableWithValues);

}
