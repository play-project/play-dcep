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
abstract public class BDPLCompilerAggregator<T extends BDPLCompilerData> {
	
	protected BDPLCompilerPhase<T> phase;
	
	public BDPLCompilerAggregator(){
		aggregate();
	}
	
	/**
	 * Get the first phase of the chain.
	 * 
	 * @return
	 */
	final public BDPLCompilerPhase<T> getPhaseChain(){
		return phase;
	}
	
	/**
	 * Create a new compiler data.
	 * 
	 * @param baseURI
	 * @param bdplQuery
	 * @return
	 */
	abstract public T createCompilerData(String baseURI, String bdplQuery);
	
	/**
	 * Build the compiler phase chain.
	 */
	abstract public void aggregate();
}
