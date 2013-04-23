package eu.play_project.dcep.distribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;


public class Main {

	private static final String propertiesFile = "proactive.java.policy";
	private static Logger logger;

	public static void main(String[] args) {
		logger = LoggerFactory.getLogger(Main.class);
		Component root = null;
		
		try {
		
			/*
			 * Set up Components
			 */
			CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(propertiesFile);
			CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
	
			Factory factory = FactoryFactory.getFactory();
			HashMap<String, Object> context = new HashMap<String, Object>();
	
			root = (Component) factory.newComponent("PlayPlatform", context);
	
			GCM.getGCMLifeCycleController(root).startFc();
	
			boolean init = false;
			
			// Wait for all subcomponents to be started
			while (!init) {
				boolean overallInitStatus = true;
				for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
					overallInitStatus = overallInitStatus && GCM.getGCMLifeCycleController(subcomponent).getFcState().equals(LifeCycleController.STARTED);
				}
				if (overallInitStatus == true) {
					init = true;
				}
				else {
					logger.info("Wait for all subcomponents to be started...");
				}
				Thread.sleep(500);
			}
					
			
			// Get interfaces
			QueryDispatchApi queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));
	
			/*
			 *  Compile and Deploy Queries
			 */
			for (String queryFileName : DcepConstants.getProperties().getProperty("dcep.startup.registerqueries").split(",")) {
				queryFileName = queryFileName.trim();
				try {
					String	queryString = getSparqlQueries(queryFileName);
					logger.info(queryString);
					queryDispatchApi.registerQuery(queryFileName, queryString);
				} catch (QueryDispatchException e) {
					logger.warn("Error registering query {} on startup: {}", queryFileName, e.getMessage());
				} catch (NullPointerException e) {
					logger.warn("Error registering query {} on startup: {}", queryFileName, e.getMessage());
				}
			}
			
			System.out.println("Press 3x RETURN to shutdown the application");
			System.in.read();
			System.in.read();
			System.in.read();
			
		} catch (IOException e){
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchInterfaceException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalLifeCycleException e) {
			logger.error(e.getMessage(), e);
		} catch (ADLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				// Stop and terminate GCM Components
				logger.info("Terminate application");
				logger.trace("Send stopFc to all components");

				if (root != null) {
					
					// Stop is recursive...
					GCM.getGCMLifeCycleController(root).stopFc();
					// Terminate is not recursive:
					for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
						GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
					}
					GCM.getGCMLifeCycleController(root).terminateGCMComponent();
				}
				
			} catch (IllegalLifeCycleException e) {
				logger.error(e.getMessage(), e);
			} catch (NoSuchInterfaceException e) {
				logger.error(e.getMessage(), e);
			}
		}

	}
	
	private static String getSparqlQueries(String queryFile) throws IOException {
		InputStream is = Main.class.getClassLoader().getResourceAsStream(queryFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String line;
		
		while (null != (line = br.readLine())) {
				sb.append(line);
				sb.append("\n");
		}
		br.close();
		is.close();

		return sb.toString();
	}
}
