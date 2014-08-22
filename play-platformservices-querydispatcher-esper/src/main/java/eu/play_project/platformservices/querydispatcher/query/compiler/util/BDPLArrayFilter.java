/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;

import java.util.Map;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.VariableBinder;

/**
 * data structure for an array filter in bdpl
 * 
 * @author ningyuan 
 * 
 * Aug 12, 2014
 *
 */
public class BDPLArrayFilter implements IBDPLFilter<Map<String, String[]>>{
	
	/*
	 * get variables or array variables used in expression during evaluating 
	 */
	private final VariableBinder vb;
	
	/*
	 * the expression of array filter
	 */
	private final IExternalFunctionExpression<VariableBinder> exp;
	
	private boolean hasVariable = false;
	
	public BDPLArrayFilter(VariableBinder variableBinder, IExternalFunctionExpression<VariableBinder> expression) throws BDPLFilterException{
		vb = variableBinder;
		
		Class ret = expression.getValueType();
		if(ret != null && !ret.getCanonicalName().equals("java.lang.Boolean")){
			throw new BDPLFilterException("Array filter is not evaluated as boolean value but "+ret.getCanonicalName());
		}
		exp = expression;
		exp.setDataObject(vb);
	}
	
	private BDPLArrayFilter(boolean hv, VariableBinder variableBinder, IExternalFunctionExpression<VariableBinder> expression){
		hasVariable = hv;
		vb = variableBinder;
		exp = expression;
		exp.setDataObject(vb);
	}
	
	/**
	 * provides a variable binding for evaluating filter expression
	 * 
	 * @param binding
	 */
	@Override
	public void setDataObject(Map<String, String[]> binding){
		vb.bindVariableValues(binding);
	}
	
	public boolean hasVariable(){
		return hasVariable;
	}
	
	public void setHasVariable(boolean b){
		hasVariable = b;
	}
	
	@Override
	public boolean evaluate() throws BDPLFilterException {
		
		return Boolean.valueOf(exp.getValue().toString());
	}

	@Override
	public IBDPLFilter<Map<String, String[]>> copy() {
		return new BDPLArrayFilter(hasVariable, new VariableBinder(vb.getArrayTable()), exp.copy());
	}
	
}
