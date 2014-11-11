/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLCompileException;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public abstract class BDPLCompilerPhase<T> {
	
	protected BDPLCompilerPhase<T> next = null;
	
	public void setNextPhase(BDPLCompilerPhase<T> next){
		this.next = next;
	}
	
	public void handle(T data) throws BDPLCompileException{
		this.process(data);
		
		if(next != null){
			next.handle(data);
		}
	}
	
	abstract protected void process(T data) throws BDPLCompileException;
}
