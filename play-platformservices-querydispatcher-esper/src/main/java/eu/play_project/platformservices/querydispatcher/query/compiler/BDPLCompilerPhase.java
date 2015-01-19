/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;


/**
 * The abstract class of a compiler phase.
 * 
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public abstract class BDPLCompilerPhase<T extends BDPLCompilerData> {
	
	/*
	 * the next phase connected to this phase
	 */
	protected BDPLCompilerPhase<T> next = null;
	
	public void setNextPhase(BDPLCompilerPhase<T> next){
		this.next = next;
	}
	
	/**
	 * Start the process of compiling from this phase.
	 * 
	 * @param data global data in the process of compiling
	 * @throws BDPLCompilerException
	 */
	public void handle(T data) throws BDPLCompilerException{
		this.process(data);
		
		if(next != null){
			next.handle(data);
		}
	}
	
	/**
	 * Execute specific actions in this phase.
	 * 
	 * @param data global data in the process of compiling
	 * @throws BDPLCompilerException
	 */
	abstract protected void process(T data) throws BDPLCompilerException;
}
