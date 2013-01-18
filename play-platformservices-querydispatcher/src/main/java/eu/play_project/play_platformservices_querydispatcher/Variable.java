package eu.play_project.play_platformservices_querydispatcher;

import java.util.ArrayList;
import java.util.List;

import eu.play_platform.platformservices.epsparql.VariableTypes;

public class Variable {
	String name;
	List<VariableTypes> types;
	List<String> values;
	int type;
	
	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	public List<String> getValues() {
		return values;
	}


	public void setValues(List<String> values) {
		this.values = values;
	}


	public Variable(String name, VariableTypes type) {
		this.name = name;
		this.types = new ArrayList<VariableTypes>();
		this.types.add(type);
		this.values =  new ArrayList<String>();
	}
	
	public Variable(String name, VariableTypes type, String value) {
		this.name = name;
		this.types = new ArrayList<VariableTypes>();
		this.types.add(type);
		this.values =  new ArrayList<String>();
		this.values.add(value);
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<VariableTypes> getTypes() {
		return types;
	}
	public void addType(VariableTypes type) {
		this.types.add(type);
	}
	
	public void addValue(String value){
		values.add(value);
	}

}
