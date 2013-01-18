package eu.play_project.dcep.distribution.tests;

import org.junit.Assert;
import org.junit.Test;

import eu.play_project.play_commons.constants.Constants;

public class PropertiesTest {

	@Test
	public void testProperties() {
		int i = 0;
		for (String queryFileName : Constants.getProperties("play-dcep-distribution.properties").getProperty("dcep.startup.registerqueries").split(",")) {
			queryFileName = queryFileName.trim();
			System.out.println("Property value found: " + queryFileName);
			i++;
		}
		Assert.assertTrue("There was no single value found in the comma-separated property.", i > 0);
	}
}
