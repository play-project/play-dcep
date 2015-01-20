/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerAggregator;
import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.GenerationPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.InitiationPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.PreparationPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.TranslationPhase;

/**
 * @author ningyuan 
 * 
 * Jan 19, 2015
 *
 */
public class DefaultBDPLCompilerAggregator extends BDPLCompilerAggregator<DefaultBDPLCompilerData>{
	
	
	
	@Override
	public void aggregate() {
		BDPLCompilerPhase<DefaultBDPLCompilerData> next, temp;
		phase = new PreparationPhase();
		next = new TranslationPhase();
		phase.setNextPhase(next);
		temp = next;
		next = new InitiationPhase();
		temp.setNextPhase(next);
		temp = next;
		next = new GenerationPhase();
		temp.setNextPhase(next);
	}


	@Override
	public DefaultBDPLCompilerData createCompilerData(String baseURI, String bdplQuery) {
		return new DefaultBDPLCompilerData(baseURI, bdplQuery);
	}

}
