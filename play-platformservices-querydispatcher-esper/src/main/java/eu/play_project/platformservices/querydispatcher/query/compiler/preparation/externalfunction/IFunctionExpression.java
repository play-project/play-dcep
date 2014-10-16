/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction;

import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;

/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public interface IFunctionExpression<T> {
	
	
	/**
	 * Set data object used for evaluating expression. This method must be called before calling getValue().
	 * 
	 * @param data
	 */
	public void setDataObject(T data);
	
	/**
	 * Get the value of this expression
	 * 
	 * @return
	 * @throws BDPLFilterException
	 */
	public Object getValue() throws BDPLFilterException;
	
	/**
	 * 
	 * @return
	 */
	public Class getValueType();
	
	public IFunctionExpression<T> copy();
}
