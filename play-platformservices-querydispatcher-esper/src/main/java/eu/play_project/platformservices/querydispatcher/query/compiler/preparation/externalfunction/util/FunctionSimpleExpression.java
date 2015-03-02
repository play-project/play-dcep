/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IFunctionExpression;

/**
 * A concrete external function expression. This expression represents a
 * simple valid value. For instance, an integer or a decimal.
 *
 *
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class FunctionSimpleExpression implements IFunctionExpression<VariableBinder> {
	/*
	 * be identical with grammar file
	 */
	public static final String VALUE_TYPE_INT = "int", VALUE_TYPE_DEC = "decimal", VALUE_TYPE_BOOLEAN = "boolean", VALUE_TYPE_STR = "str";
	
	private Class valueType;
	
	private Object value;
	
	public FunctionSimpleExpression(String type, String value) throws BDPLFilterException{
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
			valueType = String.class;
			this.value = value;
		}
		else{
			throw new BDPLFilterException("Not supported value type "+type+" in external function expression");
		}
	}
	
	/*
	 * used for copy
	 */
	private FunctionSimpleExpression(Class vt, Object v){
		valueType = vt;
		value = v;
	}
	
	@Override
	public IFunctionExpression<VariableBinder> copy(){
		return new FunctionSimpleExpression(valueType, value);
	}

	
	@Override
	public void setDataObject(VariableBinder data) {}


	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws BDPLFilterException{
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
