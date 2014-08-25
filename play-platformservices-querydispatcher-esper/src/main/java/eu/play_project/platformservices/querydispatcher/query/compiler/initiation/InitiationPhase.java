/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.ArrayInitiator;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.DefaultArrayMaker;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompilerData;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class InitiationPhase extends BDPLCompilerPhase<BDPLCompilerData> {

	@Override
	protected void process(BDPLCompilerData data) throws BDPLCompileException {
		try {
			//TODO factory pattern???
			data.setSubQueryTable(new ArrayInitiator(new DefaultArrayMaker()).initiate(data.getArrayTable()));
			
		} catch (InitiateException e) {
			throw new BDPLCompileException(e.getMessage());
		}
	}

}
