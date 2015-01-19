/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation;

import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerException;
import eu.play_project.platformservices.querydispatcher.query.compiler.BDPLCompilerPhase;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.CoordinateSystemListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener.RealTimeSolutionSequenceListener;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResultBindingData;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeSolutionSequence;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLCompilerData;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLPreparedQuery;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class GenerationPhase extends BDPLCompilerPhase<DefaultBDPLCompilerData> {

	@Override
	protected void process(DefaultBDPLCompilerData data) throws BDPLCompilerException {
		RealTimeSolutionSequence realTimeResults = new RealTimeSolutionSequence();
		
		RealTimeResultBindingData rtbData = new RealTimeResultBindingData(data.getVarTable().getRealTimeCommonVars(), realTimeResults, data.getSubQueryTable().getEntryToSelf());
		
		data.getEPLTranslationData().getInjectParameterMapping().put(data.getEPLTranslationData().INJECT_PARA_REALTIMERESULT_BINDING_DATA, rtbData);
		
		//UpdateListener listener = new RealTimeResultListener(realTimeResults, data.getEplTranslationData().getEventPatternFilters(), data.getConstructTemplate(), data.getArrayTable());
		
		//XXX coordinate listener
		UpdateListener listener = new CoordinateSystemListener(realTimeResults, data.getEPLTranslationData().getEventPatternFilters(), data.getConstructTemplate(), data.getArrayTable());
		
		data.setCompiledQuery(new DefaultBDPLPreparedQuery(data.getEPLTranslationData().getEpl(), data.getEPLTranslationData().getInjectParameterMapping(), data.getEPLTranslationData().getInjectParams(), listener, data.getSubQueryTable()));
		
	}

}
