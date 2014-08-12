/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression;

/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class ExternalFunctionVarExpression implements IExternalFunctionExpression {
	
	private final String vn;
	
	private final VariableBinder vb;
	
	public ExternalFunctionVarExpression(String varName, VariableBinder variableBinder){
		vn = varName;
		vb = variableBinder;
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws ExternalFunctionExpressionEvaluateException{
		Object ret = vb.getVar(vn);
		if(ret != null){
			return ret;
		}
		else{
			throw new ExternalFunctionExpressionEvaluateException("Value of ariable "+vn+" could not be binded during evaluating external function expression");
		}
	}

	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValueType()
	 */
	@Override
	public Class getValueType() {
		return null;
	}


}
