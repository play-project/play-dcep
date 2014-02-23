package eu.play_project.dcep.distributedetalis.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;

/**
 * Abstract test class dealing with initiation of the platform before each test.
 * Instatiating the platform is done with {@link BeforeClass} because the
 * native Prolog engine can be instatiated only once.
 */
public class SemWebLibAbstractTest {
	public static JtalisContextImpl ctx;
	static PlayJplEngineWrapper engine;
	public static final Logger logger = LoggerFactory.getLogger(SemWebLibAbstractTest.class);
	
	@BeforeClass
	public static void instantiatePlayPlatform() {
		engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		ctx = new JtalisContextImpl(engine);
		
		//Load sematic web libs.
		engine.executeGoal("[library(semweb/rdf_db)]");
	}
	
	@AfterClass
	public static void terminatePlayPlatform() {
		
	}
}
