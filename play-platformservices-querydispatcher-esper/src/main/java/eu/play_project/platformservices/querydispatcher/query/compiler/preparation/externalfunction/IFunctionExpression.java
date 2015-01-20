/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.BDPLFilterException;

/**
 * The interface of expression.
 * 
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
	 * Get the value of this expression, may recursively calculate sub expressions.
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
	
	/**
	 * Deep copy the object of this expression, may recursively copy objects of sub expressions.
	 * 
	 * @return
	 */
	public IFunctionExpression<T> copy();
}
