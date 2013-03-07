package eu.play_project.dcep.api.tests;

import org.junit.Assert;
import org.junit.Test;

import eu.play_project.dcep.constants.DcepConstants;

public class PropertiesTest {

	/**
	 * Test for some important properties to avoid {@linkplain NullPointerException}s at runtime.
	 */
	@Test
	public void testProperties() {
		int i = 0;
		for (String queryFileName : DcepConstants.getProperties().getProperty("dcep.startup.registerqueries").split(",")) {
			queryFileName = queryFileName.trim();
			System.out.println("Property value found: " + queryFileName);
			i++;
		}
		Assert.assertTrue("There was no single value found in the comma-separated property.", i > 0);
		Assert.assertNotNull("Middleware config property not found", DcepConstants.getProperties().getProperty("dcep.middleware"));
	}
}
