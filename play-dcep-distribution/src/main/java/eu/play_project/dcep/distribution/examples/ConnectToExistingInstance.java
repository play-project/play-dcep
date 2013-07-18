package eu.play_project.dcep.distribution.examples;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.naming.NamingException;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;

import com.hp.hpl.jena.graph.Node;

import eu.play_project.dcep.api.DcepManagmentApi;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;

public class ConnectToExistingInstance {
	static PublishApi dcepPublishApi;
	static DcepManagmentApi dcepManagmentApi;
	static int startTime = 0;
	static Timer timer;
	boolean timeUp = false;
	public static int sendetEvents = 0;

	public static void main(String[] args) throws RemoteException {
		
		// Get connection
		connectToCepEngine(URIBuilder.buildURI("141.52.218.16", "dEtalis", "rmi", 1099).toString());
		// connectToCepEngine(URIBuilder.buildURI("2001:6f8:100d:b::1", "dEtalis", "rmi", 1099).toString());

	}

	private static void connectToCepEngine(String url) {
		/* COMPONENT_ALIAS = "Dispatcher" */
		PAComponentRepresentative root = null;

		// root = Fractive.lookup(URIBuilder.buildURI("2001:6f8:100d:b::1",
		// "dEtalis2", "rmi", 1099).toString());
		try {
			root = Fractive.lookup(url);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}

		try {
			dcepPublishApi = ((fr.inria.eventcloud.api.PublishApi) root
					.getFcInterface("PublishApi"));
			dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root
					.getFcInterface("DcepManagmentApi"));
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		// Register query
		// dcepManagmentApi.registerEventPattern(generateEle(getSparqlQuerys("3timesA.eprq")));

		System.out.println("t_1: \t" + System.currentTimeMillis());
	}

	public static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static CompoundEvent createEvent(String eventId, int value,
			String type) {

		List quads = new ArrayList();

		Quadruple q1 = new Quadruple(
				Node.createURI("http://prefix.example.com/" + eventId),
				Node.createURI("http://prefix.example.com/e1"),
				Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				Node.createURI("http://prefix.example.com/" + type));

		Quadruple q2 = new Quadruple(
				Node.createURI("http://prefix.example.com/" + eventId),
				Node.createURI("http://prefix.example.com/e1"),
				Node.createURI("http://prefix.example.com/value"),
				Node.createURI(System.currentTimeMillis() + ""));

		Quadruple q3 = new Quadruple(
				Node.createURI("http://prefix.example.com/" + eventId),
				Node.createURI("http://prefix.example.com/e1"),
				Node.createURI("http://prefix.example.com/math/value"),
				Node.createURI(value + ""));

		quads.add(q1);
		quads.add(q3);
		quads.add(q2);

		return new CompoundEvent(quads);
	}

	public void timeIsUp() {

	}

}
