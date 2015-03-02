/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import java.util.Map;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IFunctionExpression;

/**
 * The data structure for an array filter in bdpl. An array filter
 * is composed of an function expression which represents the actual
 * array filter and a variable binder which binds the latest values
 * of all variables used in the array filter.
 * 
 * 
 * 
 * @author ningyuan 
 * 
 * Aug 12, 2014
 *
 */
public class BDPLArrayFilter{
	
	/*
	 * get variables or array variables used in expression during evaluating 
	 */
	private final VariableBinder vb;
	
	/*
	 * the expression of the array filter
	 */
	private final IFunctionExpression<VariableBinder> exp;
	
	/*
	 * indicate whether the array filter contains any variable
	 */
	private boolean hasVariable = false;
	
	public BDPLArrayFilter(VariableBinder variableBinder, IFunctionExpression<VariableBinder> expression) throws BDPLFilterException{
		vb = variableBinder;
		
		Class ret = expression.getValueType();
		if(ret != null && !ret.getCanonicalName().equals("java.lang.Boolean")){
			throw new BDPLFilterException("Array filter is not evaluated as boolean value but "+ret.getCanonicalName());
		}
		
		exp = expression;
		exp.setDataObject(vb);
	}
	
	/*
	 * this constructor is used when copy this array filter object
	 */
	private BDPLArrayFilter(boolean hv, VariableBinder variableBinder, IFunctionExpression<VariableBinder> expression){
		hasVariable = hv;
		vb = variableBinder;
		exp = expression;
		exp.setDataObject(vb);
	}
	
	/**
	 * Provides a variable binding for evaluating filter expression
	 * 
	 * @param binding
	 */
	public void setDataObject(Map<String, String[]> binding, Map<String, String[][][]> dArrays){
		vb.bindVariableValues(binding, dArrays);
	}
	
	/**
	 * Test whether the array filter contains any variable.
	 * 
	 * @return
	 */
	public boolean hasVariable(){
		return hasVariable;
	}
	
	/**
	 * Set the flag indicating whether the array filter contains any variable.
	 * 
	 * @param b
	 */
	public void setHasVariable(boolean b){
		hasVariable = b;
	}
	
	/**
	 * Evaluate the array filter.
	 * 
	 * @return
	 * @throws BDPLFilterException
	 */
	public boolean evaluate() throws BDPLFilterException {
		
		return Boolean.valueOf(exp.getValue().toString());
	}
	
	/**
	 * Deep copy the whole object of the array filter.
	 * 
	 * @return
	 */
	public BDPLArrayFilter copy() {
		return new BDPLArrayFilter(hasVariable, new VariableBinder(vb.getArrayTable()), exp.copy());
	}
	
}
