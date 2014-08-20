/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.ExternalFunctionExpressionEvaluateException;

/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public interface IExternalFunctionExpression<T> {
	
	
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
	 * @throws ExternalFunctionExpressionEvaluateException
	 */
	public Object getValue() throws ExternalFunctionExpressionEvaluateException;
	
	/**
	 * 
	 * @return
	 */
	public Class getValueType();
}
