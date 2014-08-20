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
	private final IExternalFunctionExpression<VariableBinder> exp;
	
	public ArrayFilter(VariableBinder variableBinder, IExternalFunctionExpression<VariableBinder> expression) throws ExternalFunctionExpressionEvaluateException{
		vb = variableBinder;
		
		Class ret = expression.getValueType();
		if(ret != null && !ret.getCanonicalName().equals("java.lang.Boolean")){
			throw new ExternalFunctionExpressionEvaluateException("Array filter is not evaluated as boolean value but "+ret.getCanonicalName());
		}
		exp = expression;
		exp.setDataObject(vb);
	}
	
	/**
	 * provides a variable binding for evaluating filter expression
	 * 
	 * @param binding
	 */
	public void setVariableBinding(Map<String, String[]> binding){
		vb.bindVariableValues(binding);
	}
	
	
	public boolean evaluate() throws ExternalFunctionExpressionEvaluateException {
		
		return Boolean.valueOf(exp.getValue().toString());
	}
	
}
