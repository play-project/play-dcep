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
public interface IExternalFunctionExpression {
	
	public Object getValue() throws ExternalFunctionExpressionEvaluateException;
	
	public Class getValueType();
}
