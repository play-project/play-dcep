package eu.play_project.dcep.distributedetalis.utils;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;

/**
 * Utility class for using ProActive and GCM.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 */
public class ProActiveHelpers {

	private static Logger logger = LoggerFactory.getLogger(ProActiveHelpers.class);

	/**
	 * Create a new {@link Component} using configuration properties from the
	 * DCEP configuration files.
	 */
	public static Component newComponent(String componentName, Map<String, Object> context) throws ADLException {
		
		final int PROACTIVE_PNP_PORT = Integer.parseInt(DcepConstants.getProperties().getProperty("dcep.proactive.pnp.port"));
		final int PROACTIVE_HTTP_PORT = Integer.parseInt(DcepConstants.getProperties().getProperty("dcep.proactive.http.port"));
		final int PROACTIVE_RMI_PORT = Integer.parseInt(DcepConstants.getProperties().getProperty("dcep.proactive.rmi.port"));

		logger.debug("Setting property '{}' to: ", PNPConfig.PA_PNP_PORT.getName(), PROACTIVE_PNP_PORT);
		PNPConfig.PA_PNP_PORT.setValue(PROACTIVE_PNP_PORT);
		
		logger.debug("Setting property '{}' to: ", CentralPAPropertyRepository.PA_XMLHTTP_PORT.getName(), PROACTIVE_HTTP_PORT);
		CentralPAPropertyRepository.PA_XMLHTTP_PORT.setValue(PROACTIVE_HTTP_PORT);
		
		logger.debug("Setting property '{}' to: ", CentralPAPropertyRepository.PA_RMI_PORT.getName(), PROACTIVE_RMI_PORT);
		CentralPAPropertyRepository.PA_RMI_PORT.setValue(PROACTIVE_RMI_PORT);

		Factory factory = FactoryFactory.getFactory();
		
		return (Component) factory.newComponent(componentName, context);
	}
	
	/**
	 * Create a new {@link Component} using configuration properties from the
	 * DCEP configuration files.
	 */
	public static Component newComponent(String componentName) throws ADLException {
		HashMap<String, Object> context = new HashMap<String, Object>();
		return newComponent(componentName, context);
	}

}
