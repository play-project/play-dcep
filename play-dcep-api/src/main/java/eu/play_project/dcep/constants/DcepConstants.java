package eu.play_project.dcep.constants;

import java.util.Properties;

import eu.play_project.play_commons.constants.Constants;

public class DcepConstants extends Constants {

	private static Properties properties;
	private static final String PROPERTIES = "play-dcep-distribution.properties";
	private static final String PROPERTIES_DEFAULTS = "play-dcep-distribution-defaults.properties";

	public static Properties getProperties() {
		if (properties == null) {
			properties = Constants.getProperties(PROPERTIES,
					Constants.getProperties(PROPERTIES_DEFAULTS));
		}
		return properties;
	}

}
