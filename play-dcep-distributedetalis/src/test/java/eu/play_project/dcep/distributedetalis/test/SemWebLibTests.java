package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.junit.Test;

import com.jtalis.core.JtalisContextImpl;
import com.jtalis.core.plengine.PrologEngineWrapper;

import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;


public class SemWebLibTests {
	private JtalisContextImpl ctx;
	
	/**
	 * Get all subclasses of a given class. Class hierarchy is loaded from file.
	 */
	@Test
	public void rdfsSubclassExampleTest() throws IOException{
		
		PlayJplEngineWrapper engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		this.ctx = new JtalisContextImpl(engine);
		
		//Load sematic web libs.
		engine.executeGoal("[library(semweb/rdf_db)]");
		engine.executeGoal("[library(semweb/rdfs)]");

		// Load class hierarchy.
		//FIXME find a method to load it in java.
		ctx.getEngineWrapper().executeGoal("rdf_load('" + new File (".").getCanonicalPath().replace("\\", "/") + "/src/test/resources/event_types.xml')");
		System.out.println("rdf_load('" + new File (".").getCanonicalPath().replace("\\", "/") + "/src/test/resources/event_types.xml')");
		// Get all Telco subclasses.
		Hashtable<String, Object>[] result =  engine.execute("rdf(X, 'http://www.w3.org/2000/01/rdf-schema#subClassOf', 'http://events.event-processing.org/types/Telco').");
		
		String[] expectedClasses = {"'http://events.event-processing.org/types/ContextualizedLatitudeEvent'","'http://events.event-processing.org/types/UcTelcoAnswer'","'http://events.event-processing.org/types/UcTelcoAvailability'","'http://events.event-processing.org/types/UcTelcoCall'","'http://events.event-processing.org/types/UcTelcoClic2Call'","'http://events.event-processing.org/types/UcTelcoEsrRecom'","'http://events.event-processing.org/types/UcTelcoGeoLocation'","'http://events.event-processing.org/types/UcTelcoOutNetwork'","'http://events.event-processing.org/types/UcTelcoPresence'","'http://events.event-processing.org/types/UcTelcoSmsCustomerAlert'","'http://events.event-processing.org/types/UcTelcoTrafficJam'","'http://events.event-processing.org/types/UcTelcoUnexpected'"};
		
		// Compare result wit expected result.
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].get("X").toString().getBytes().length; j++) {
				if(result[i].get("X").toString().getBytes()[j] != expectedClasses[i].getBytes()[j] ){
					fail();
				}
			}
			
		}
		
	}

}
