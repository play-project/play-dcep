package eu.play_project.querydispatcher.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;

public class UniqueNameManagerTest {
	
	@Test
	public void testResetMethod() {
		UniqueNameManager unm = UniqueNameManager.getVarNameManager();
		
		
		// Simulate code generation for different patterns. 
		unm.newQuery();
		assertEquals(unm.getCurrentSimpleEventNumber(), 0);
		
		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 1);
		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 2);
		
		unm.resetTriplestoreVariable();
		assertEquals(unm.getCurrentSimpleEventNumber(), 1);
		
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(0), "ViD1");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(1), "ViD2");
		
		
		
		
		// Process new query.
		unm.newQuery();
		assertEquals(unm.getCurrentSimpleEventNumber(), 2);
		
		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 3);
		
		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 4);
		
		unm.resetTriplestoreVariable();
		assertEquals(unm.getCurrentSimpleEventNumber(), 3);
		
		unm.processNextEvent();
		unm.processNextEvent();
		unm.processNextEvent();
		
		unm.resetTriplestoreVariable();
		assertEquals(unm.getCurrentSimpleEventNumber(), 3);
		
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(0), "ViD3");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(1), "ViD4");
		
	}

}
