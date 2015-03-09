/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

import eu.play_project.platformservices.querydispatcher.query.compiler.util.DefaultBDPLCompilerAggregator;

/**
 * @author ningyuan 
 * 
 * Jan 19, 2015
 *
 */
public class BDPLCompilerFactory {
	
	public static final String TYPE_DEFAULT = "default";
	
	/*
	 * the concrete aggregator class for build compiler phase chain and compiler data
	 */
	private final BDPLCompilerAggregator compilerAggregator;
	
	public BDPLCompilerFactory(String aggregator){
		
		if(aggregator != null){
			if(aggregator.equals(TYPE_DEFAULT)){
				compilerAggregator = new DefaultBDPLCompilerAggregator();
			}
			//TODO other aggregators
			else{
				compilerAggregator = new DefaultBDPLCompilerAggregator();
			}
		}
		else{
			compilerAggregator = new DefaultBDPLCompilerAggregator();
		}
		
	}
	
	public BDPLCompilerPhase getPhaseChain(){
		return compilerAggregator.getPhaseChain();
	}
	
	public BDPLCompilerData getCompilerData(String baseURI, String bdplQuery){
		return compilerAggregator.createCompilerData(baseURI, bdplQuery);
	}
}
