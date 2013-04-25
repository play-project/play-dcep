package eu.play_project.play_platformservices_querydispatcher;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.graph.Node_Variable;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.api.QueryDispatechElement;
import eu.play_project.play_platformservices_querydispatcher.api.QueryDispatechElementVisitor;

public class VariableWithValues extends Node_Variable implements QueryDispatechElement {
	private Node_Variable variable;
	ArrayList<String> values;
	List<VariableTypes> types;
	
	protected VariableWithValues(Object name) {
		super(name);
	}

	public VariableWithValues(String name) {
		super(name);
	}
	

	public Node_Variable getVariable() {
		return variable;
	}

	public List<VariableTypes> getTypes() {
		return types;
	}
	public void addType(VariableTypes type) {
		this.types.add(type);
	}
	public void setVariable(Node_Variable variable) {
		this.variable = variable;
	}

	public void addValue(String value) {
		values.add(value);
	}

	public ArrayList<String> getValues() {
		return values;
	}

	@Override
	public String getName() {
		return variable.getName();
	}

	@Override
	public Object visitWith(NodeVisitor v) {
		return variable.visitWith(v);
	}

	@Override
	public boolean isVariable() {
		return variable.isVariable();
	}

	@Override
	public String toString() {
		return variable.toString();
	}

	@Override
	public boolean equals(Object other) {
		return variable.equals(other);
	}
	
	public static Object variable(String name) {
		return variable(name);
	}


	@Override
	public void accept(QueryDispatechElementVisitor visitor) {
		visitor.visit(this);
	}



}
