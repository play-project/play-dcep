/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

/**
 * The interface of a compiled BDPL query.
 * 
 * @author ningyuan 
 * 
 * Jul 29, 2014
 *
 */
public interface IBDPLQuery {
	
	public void start();
	
	public void stop();
	
	public void close();
}
