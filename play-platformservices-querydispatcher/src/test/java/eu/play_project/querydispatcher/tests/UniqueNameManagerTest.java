package eu.play_project.querydispatcher.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;

public class UniqueNameManagerTest {

	@Test
	public void TestResetMethod() {
		UniqueNameManager unm = UniqueNameManager.getVarNameManager();

		// Simulate code generation for different patterns.
		unm.newQuery();
		assertEquals(unm.getCurrentSimpleEventNumber(), 1);

		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 2);
		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 3);

		unm.resetTriplestoreVariable();
		assertEquals(unm.getCurrentSimpleEventNumber(), 1);

		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(0), "ViD2");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(1), "ViD3");

		// Process new query.
		unm.newQuery();
		assertEquals(unm.getCurrentSimpleEventNumber(), 4);

		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 5);

		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 6);

		unm.resetTriplestoreVariable();
		assertEquals(unm.getCurrentSimpleEventNumber(), 4);

		unm.processNextEvent();
		unm.processNextEvent();
		unm.processNextEvent();

		unm.resetTriplestoreVariable();
		assertEquals(unm.getCurrentSimpleEventNumber(), 4);

		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(0), "ViD5");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(1), "ViD6");

	}

}