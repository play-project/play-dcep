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
	
	/*
	 * the concrete aggregator class for build compiler phase chain and compiler data
	 */
	private final IBDPLCompilerAggregator compilerAggregator;
	
	public BDPLCompilerFactory(String aggregator){
		//XXX use aggregator name to initiate different compiler
		
		compilerAggregator = new DefaultBDPLCompilerAggregator();
		
	}
	
	public BDPLCompilerPhase getPhaseChain(){
		return compilerAggregator.getPhaseChain();
	}
	
	public BDPLCompilerData getCompilerData(String baseURI, String bdplQuery){
		return compilerAggregator.createCompilerData(baseURI, bdplQuery);
	}
}
