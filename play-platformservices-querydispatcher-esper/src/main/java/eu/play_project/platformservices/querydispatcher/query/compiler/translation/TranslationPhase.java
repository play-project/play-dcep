/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.realtime.EPLTranslationProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TranslateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompilerData;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class TranslationPhase extends BDPLCompilerPhase<BDPLCompilerData> {
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase#process()
	 */
	@Override
	protected void process(BDPLCompilerData data) throws BDPLCompileException {
		
		try {
			data.setEPLTranslationData(EPLTranslationProcessor.process(data.getQueryContainer(), data.getPrologText()));
			
		} catch (TranslateException e) {
			throw new BDPLCompileException(e.getMessage());
		}
	}

}
