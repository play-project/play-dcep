/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerException;
import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.ConstructTranslationProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.realtime.EPLTranslationProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TranslateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLCompilerData;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class TranslationPhase extends BDPLCompilerPhase<DefaultBDPLCompilerData> {
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase#process()
	 */
	@Override
	protected void process(DefaultBDPLCompilerData data) throws BDPLCompilerException {
		
		try {
			data.setConstructTemplate(ConstructTranslationProcessor.process(data.getQueryContainer(), data.getArrayTable()));
			data.setEPLTranslationData(EPLTranslationProcessor.process(data.getQueryContainer(), data.getPrologText()));
			
		} catch (TranslateException e) {
			throw new BDPLCompilerException(e.getMessage());
		}
	}

}
