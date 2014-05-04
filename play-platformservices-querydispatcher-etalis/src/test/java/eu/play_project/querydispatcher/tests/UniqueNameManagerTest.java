package eu.play_project.querydispatcher.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;

public class UniqueNameManagerTest {

	@Test
	public void TestResetMethod() {
		UniqueNameManager unm = UniqueNameManager.getVarNameManager();
		unm = unm.reset();


		// Simulate code generation for different patterns.
		unm.newQuery(2);
		assertEquals(0, unm.getCurrentSimpleEventNumber());

		unm.processNextEvent();
		assertEquals(1, unm.getCurrentSimpleEventNumber());
		unm.processNextEvent();
		assertEquals(unm.getCurrentSimpleEventNumber(), 2);

		unm.resetTriplestoreVariable();
		assertEquals(unm.getCurrentSimpleEventNumber(), 0);

		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(0), "ViD1");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(1), "ViD2");

		// Process new query.
		unm.newQuery(5);
		assertEquals(2, unm.getCurrentSimpleEventNumber());

		unm.processNextEvent();
		assertEquals(3, unm.getCurrentSimpleEventNumber());

		unm.processNextEvent();
		assertEquals(4, unm.getCurrentSimpleEventNumber());

		unm.resetTriplestoreVariable();
		assertEquals(2, unm.getCurrentSimpleEventNumber());

		unm.processNextEvent();
		unm.processNextEvent();
		unm.processNextEvent();

		unm.resetTriplestoreVariable();
		assertEquals(2, unm.getCurrentSimpleEventNumber());

		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(0), "ViD3");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(1), "ViD4");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(2), "ViD5");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(3), "ViD6");
		assertEquals(unm.getAllTripleStoreVariablesOfThisQuery().get(4), "ViD7");
	}

}