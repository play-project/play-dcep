package eu.play_project.dcep.distributedetalis.tests.prolog;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.jtalis.core.JtalisContext;
import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;


public class PrologAbstractTest {
	static PlayJplEngineWrapper engine;
	static JtalisContext context;
	
	@BeforeClass
	public static void instantiatePlayPlatform() {
		// Init ETALIS
		engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		context = new JtalisContextImpl(engine);
		
		engine.executeGoal("[library(semweb/rdf_db)]");
	}
	
	@AfterClass
	public static void terminatePlayPlatform() {
		engine.shutdown();
	}

}
