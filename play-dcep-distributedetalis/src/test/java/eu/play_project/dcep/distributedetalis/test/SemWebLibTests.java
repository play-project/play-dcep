package eu.play_project.dcep.distributedetalis.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jtalis.core.JtalisContextImpl;
import com.jtalis.core.plengine.PrologEngineWrapper;

import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;


public class SemWebLibTests {
	private JtalisContextImpl ctx;
	
	@Test
	public void rdfsSubclassExampleTest(){
		
		PrologEngineWrapper<?> engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		this.ctx = new JtalisContextImpl(engine);
		
		//Load libs.
		ctx.getEngineWrapper().executeGoal("[library(semweb/rdf_db)]");
		ctx.getEngineWrapper().executeGoal("[library(semweb/rdfs)]");
		
		
		ctx.getEngineWrapper().executeGoal("rdf_assert('MeasurementEvent', rdfs:subClassOf, 'Event')");
		ctx.getEngineWrapper().executeGoal("rdf_assert('PrecipitationEvent', rdfs:subClassOf, 'MeasurementEvent')");
		boolean result = ctx.getEngineWrapper().executeGoal("rdfs_subclass_of('PrecipitationEvent', 'Event'");
		
		assertTrue(result);
		
		
	}

}
