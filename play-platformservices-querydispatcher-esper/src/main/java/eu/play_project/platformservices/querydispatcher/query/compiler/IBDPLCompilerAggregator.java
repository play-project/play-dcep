/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

/**
 * The interface of all aggregators of BDPL compilers. An aggregator
 * do the concrete jobs of building a compiler phase chain and compiler
 * data.
 * 
 * @author ningyuan 
 * 
 * Jan 19, 2015
 *
 */
public interface IBDPLCompilerAggregator {
	
	/**
	 * Build the compiler phase chain.
	 */
	public void aggregate();
	
	/**
	 * Get the first phase of the chain.
	 * 
	 * @return
	 */
	public BDPLCompilerPhase getPhaseChain();
	
	/**
	 * Create a new compiler data.
	 * 
	 * @param baseURI
	 * @param bdplQuery
	 * @return
	 */
	public BDPLCompilerData createCompilerData(String baseURI, String bdplQuery);
}
