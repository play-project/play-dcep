/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction;

import java.util.Map;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.ExternalFunctionExpressionEvaluateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.VariableBinder;

/**
 * data structure for an array filter in bdpl
 * 
 * @author ningyuan 
 * 
 * Aug 12, 2014
 *
 */
public class ArrayFilter {
	
	/*
	 * get variables or array variables used in expression during evaluating 
	 */
	private final VariableBinder vb;
	
	/*
	 * the expression of array filter
	 */
	private IExternalFunctionExpression exp;
	
	public ArrayFilter(VariableBinder variableBinder){
		vb = variableBinder;
	}
	
	public void setVariableBinding(Map<String, String[]> binding){
		vb.setVars(binding);
	}
	
	public boolean evaluate() throws ExternalFunctionExpressionEvaluateException {
		Object ret = exp.getValue();
		
		if(ret != null && ret instanceof Boolean){
			return (Boolean) ret;
		}
		else{
			throw new ExternalFunctionExpressionEvaluateException("External function expression is not evaluated as boolean value");
		}
	}
	
	void setExpression(IExternalFunctionExpression expression){
		exp = expression;
	}
}
