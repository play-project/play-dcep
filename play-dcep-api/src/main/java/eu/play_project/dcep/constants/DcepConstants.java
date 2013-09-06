package eu.play_project.dcep.constants;

import java.util.Properties;

import eu.play_project.play_commons.constants.Constants;

/**
 * Properties for DCEP. This class reads the file
 * {@code play-dcep-distribution.properties} from the classpath and also has a
 * default file included in the JAR. If a property is not found in these files
 * the class further falls back to look in
 * {@linkplain Constants#getProperties()}. You can create your own properties
 * file by adapting
 * <a href="https://github.com/play-project/play-dcep/blob/master/play-dcep-api/src/main/resources/play-dcep-distribution-defaults.properties"> the defaults</a>.
 * 
 * @author Roland Stühmer
 */
public class DcepConstants extends Constants {

	private static Properties properties;
	private static final String PROPERTIES = "play-dcep-distribution.properties";
	private static final String PROPERTIES_DEFAULTS = "play-dcep-distribution-defaults.properties";

	public static Properties getProperties() {
		if (properties == null) {
			properties = Constants.getProperties(PROPERTIES,Constants.getProperties(PROPERTIES_DEFAULTS, Constants.getProperties()));
		}
		return properties;
	}

}
