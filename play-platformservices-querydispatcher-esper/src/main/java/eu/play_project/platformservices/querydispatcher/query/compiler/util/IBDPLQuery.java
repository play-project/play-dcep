/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;

/**
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
