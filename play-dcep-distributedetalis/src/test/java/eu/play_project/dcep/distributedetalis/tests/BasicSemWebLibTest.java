package eu.play_project.dcep.distributedetalis.tests;

import org.junit.Test;

public class BasicSemWebLibTest extends SemWebLibAbstractTest {
	
	@Test
	public void assertDataTest() {
		ctx.getEngineWrapper().executeGoal("write(adddd\n)");

		
	}
}
