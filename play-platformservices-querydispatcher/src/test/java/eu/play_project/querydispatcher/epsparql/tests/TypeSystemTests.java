package eu.play_project.querydispatcher.epsparql.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;

public class TypeSystemTests {
	
	/**
	 * Set different types and retrieve them.
	 */
	@Test
	public void findType(){
		VariableTypeManager vm = new VariableTypeManager(null);
		
		// Set types.
		vm.addVariable("a", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("a", VariableTypes.AVG_TYPE);
		
		// Check if it is possible to retrieve informations.
		assertTrue(vm.isType("a", VariableTypes.CONSTRUCT_TYPE));
		assertTrue(vm.isType("a", VariableTypes.AVG_TYPE));
		
		assertFalse(vm.isType("a", VariableTypes.HISTORIC_TYPE));
		assertFalse(vm.isType("a", VariableTypes.REALTIME_TYPE));
		assertFalse(vm.isType("a", VariableTypes.MIN_TYPE));
	}
	
	@Test
	public void getAllVariablesOfOneType(){
		VariableTypeManager vm = new VariableTypeManager(null);
		
		// Set types.
		vm.addVariable("a", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("a", VariableTypes.REALTIME_TYPE);
		vm.addVariable("a", VariableTypes.AVG_TYPE);
		
		vm.addVariable("b", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("b", VariableTypes.REALTIME_TYPE);
		vm.addVariable("b", VariableTypes.MIN_TYPE);
		
		vm.addVariable("c", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("c", VariableTypes.REALTIME_TYPE);
		vm.addVariable("c", VariableTypes.MAX_TYPE);
		
		List<String> vars = vm.getVariables(VariableTypes.REALTIME_TYPE);
		assertTrue(vars.size()==3);
		assertTrue(vars.contains("a"));
		assertTrue(vars.contains("b"));
		assertTrue(vars.contains("c"));
		
		vars = vm.getVariables(VariableTypes.MAX_TYPE);
		assertTrue(vars.size()==1);
		assertTrue(vars.contains("c"));	
	}

}
