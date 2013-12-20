package eu.play_project.play_platformservices.tests;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.PlayPlatformservices;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

public class PlatformservicesTest {

	@Test
	public void testCxfSoap() {
		
		PlayPlatformservices playPlatformservices = new PlayPlatformservices();
		playPlatformservices.initComponentActivity(null);
		playPlatformservices.endComponentActivity(null);
		
	}
	
	@Test
	public void testPlatformservicesComponent() throws ADLException, IllegalLifeCycleException,
			NoSuchInterfaceException, InterruptedException, QueryDispatchException {
		/*
		 * Start Platformservices server
		 */
		Component root = ProActiveHelpers.newComponent("PlatformServicesTest");
		GCM.getGCMLifeCycleController(root).startFc();
		
		/*
		 * Start client and get WSDL
		 */
		URL wsdl = null;
		String address = Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint");
		
		try {
			wsdl = new URL(address + "?wsdl");
		} catch (MalformedURLException e) {
		e.printStackTrace();
		}

		QName serviceName = new QName("http://play_platformservices.play_project.eu/", QueryDispatchApi.class.getSimpleName());

		Service service = Service.create(wsdl, serviceName);
		service.getPort(eu.play_project.play_platformservices.api.QueryDispatchApi.class);

		
		/*
		 * Stop server
		 */
		GCM.getGCMLifeCycleController(root).stopFc();
		GCM.getGCMLifeCycleController(root).terminateGCMComponent();
	}

}
