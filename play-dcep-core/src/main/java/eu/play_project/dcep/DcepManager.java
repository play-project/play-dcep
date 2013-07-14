package eu.play_project.dcep;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;

import eu.play_project.dcep.api.DcepManagmentApi;

/**
 * Manage dEtalis instances.
 * @author sobermeier
 *
 */
public class DcepManager {
	List<PAComponentRepresentative>  dEtalis; // Mapping between instance name and instance.
	int lastUsedNode;
	
	DcepManager(){
		dEtalis = new LinkedList<PAComponentRepresentative>();
	}
	
	/**
	 * Instantiate dEtalises.
	 */
	public void init() {
		//TODO read deployment from file or ...
		String destinations[] = {"127.0.0.1"};
		
		for (int i = 0; i < destinations.length; i++) {
			try {
				dEtalis.add(createInstance("dEtalis", destinations[0]));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}	
	}
	
	private PAComponentRepresentative createInstance(String name, String host) throws IOException, NamingException{
		return Fractive.lookup(URIBuilder.buildURI(host, name, "rmi", 1099).toString());
	}
	
	/**
	 * Get DcepManagmentApi from one instance after the other.
	 * @return Proxy to dEtalis instance.
	 */
	public DcepManagmentApi getManagementApi(){
		lastUsedNode++;
		try {
			return (DcepManagmentApi)dEtalis.get(lastUsedNode%dEtalis.size()).getFcInterface("DcepManagmentApi");
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			return null;
		}
	}
}
	



