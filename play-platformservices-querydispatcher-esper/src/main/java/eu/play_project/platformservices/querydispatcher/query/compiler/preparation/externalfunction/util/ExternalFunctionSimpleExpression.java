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
public class ExternalFunctionSimpleExpression implements IExternalFunctionExpression<VariableBinder> {
	/*
	 * be identical with grammar file
	 */
	public static final String VALUE_TYPE_INT = "int", VALUE_TYPE_DEC = "decimal", VALUE_TYPE_BOOLEAN = "boolean", VALUE_TYPE_STR = "str";
	
	private Class valueType;
	
	private Object value;
	
	public ExternalFunctionSimpleExpression(String type, String value) throws ExternalFunctionExpressionEvaluateException{
		if(type.equalsIgnoreCase(VALUE_TYPE_INT)){
			valueType = Integer.class;
			this.value = Integer.valueOf(value);
		}
		else if(type.equalsIgnoreCase(VALUE_TYPE_DEC)){
			valueType = Double.class;
			this.value = Double.valueOf(value);
		}
		else if(type.equalsIgnoreCase(VALUE_TYPE_BOOLEAN)){
			valueType = Boolean.class;
			this.value = Boolean.valueOf(value);
		}
		else if(type.equalsIgnoreCase(VALUE_TYPE_STR)){
			//TODO check
		}
		else{
			throw new ExternalFunctionExpressionEvaluateException("Not supported value type "+type+" in external function expression");
		}
	}
	
	@Override
	public void setDataObject(VariableBinder data) {}


	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws ExternalFunctionExpressionEvaluateException{
		return value;
	}

	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValueType()
	 */
	@Override
	public Class getValueType() {
		return valueType;
	}

}
