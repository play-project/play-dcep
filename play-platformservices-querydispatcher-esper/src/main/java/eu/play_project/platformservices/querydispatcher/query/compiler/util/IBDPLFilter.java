/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;

/**
 * @author ningyuan 
 * 
 * Aug 21, 2014
 *
 */
public interface IBDPLFilter<T> {
	
	public boolean evaluate() throws BDPLFilterException;
	
	public void setDataObject(T data);
	
	public IBDPLFilter<T> copy();
}
