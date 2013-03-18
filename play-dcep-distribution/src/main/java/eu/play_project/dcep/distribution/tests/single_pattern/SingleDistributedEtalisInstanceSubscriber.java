package eu.play_project.dcep.distribution.tests.single_pattern;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import javax.naming.NamingException;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.URIBuilder;

import eu.play_project.dcep.distributedetalis.api.ConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;

public class SingleDistributedEtalisInstanceSubscriber {

	private static ComplexEventSubscriber subscriber = null;
	private static DistributedEtalisTestApi testApiI1;

	public static void main(String[] args) throws ADLException,
			IllegalLifeCycleException, NoSuchInterfaceException,
			ProActiveException, DistributedEtalisException, IOException, NamingException {

		// Connect to DistributedEtalis instance.
		PAComponentRepresentative root1 = Fractive.lookup(URIBuilder.buildURI(args[0], args[1], "rmi", 1099).toString());

		//Get interfaces.
		testApiI1 = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root1.getFcInterface("DistributedEtalisTestApi"));
		
		//Subscribe
		testApiI1.attach(new ComplexEventSubscriber());
	}
}
