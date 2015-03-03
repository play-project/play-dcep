/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerException;
import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.ArrayInitiator;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.ArrayMakerFactory;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLCompilerData;

/**
 * The initiation phase of the BDPL compiler.
 * 
 * 
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class InitiationPhase extends BDPLCompilerPhase<DefaultBDPLCompilerData> {

	@Override
	protected void process(DefaultBDPLCompilerData data) throws BDPLCompilerException {
		try {
			
			data.setSubQueryTable(new ArrayInitiator(ArrayMakerFactory.TYPE_DEFAULT).initiate(data.getArrayTable()));
			
		} catch (InitiateException e) {
			throw new BDPLCompilerException(e.getMessage());
		}
	}

}
