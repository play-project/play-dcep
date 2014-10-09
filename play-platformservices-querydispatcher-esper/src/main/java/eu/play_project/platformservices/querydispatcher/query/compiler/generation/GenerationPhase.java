/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation;

import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.CoordinateListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.RealTimeResultListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResultBindingData;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResults;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompilerData;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLPreparedQuery;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class GenerationPhase extends BDPLCompilerPhase<BDPLCompilerData> {

	@Override
	protected void process(BDPLCompilerData data) throws BDPLCompileException {
		RealTimeResults realTimeResults = new RealTimeResults();
		
		RealTimeResultBindingData rtbData = new RealTimeResultBindingData(data.getVarTable().getRealTimeCommonVars(), realTimeResults, data.getSubQueryTable().getEntryToSelf());
		
		data.getEplTranslationData().getInjectParameterMapping().put(data.getEplTranslationData().INJECT_PARA_REALTIMERESULT_BINDING_DATA, rtbData);
		
		//UpdateListener listener = new RealTimeResultListener(realTimeResults, data.getEplTranslationData().getEventPatternFilters());
		
		//XXX coordinate listener
		UpdateListener listener = new CoordinateListener(realTimeResults, data.getEplTranslationData().getEventPatternFilters(), data.getArrayTable());
		
		data.setCompiledQuery(new DefaultBDPLPreparedQuery(data.getEplTranslationData().getEpl(), data.getEplTranslationData().getInjectParameterMapping(), data.getEplTranslationData().getInjectParams(), listener, data.getSubQueryTable()));
		
	}

}
