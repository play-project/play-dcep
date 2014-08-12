/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import java.util.ArrayList;
import java.util.List;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression;

/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class ExternalFunctionCompoundExpression implements IExternalFunctionExpression {
	private Class valueType;
	
	private final String operator;
	
	private List<IExternalFunctionExpression> operands = new ArrayList<IExternalFunctionExpression>();
	
	public ExternalFunctionCompoundExpression(String o){
		operator = o;
	}
	
	public void addOperand(IExternalFunctionExpression op){
		operands.add(op);
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws ExternalFunctionExpressionEvaluateException{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValueType()
	 */
	@Override
	public Class getValueType() {
		
		return valueType;
	
	}

}
