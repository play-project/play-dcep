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
public class ExternalFunctionCompoundExpression implements IExternalFunctionExpression<VariableBinder> {
	
	private static final String OP_EQ = "=", OP_NE = "!=", OP_GT = ">", OP_LT = "<", OP_GE = ">=", OP_LE = "<=",
			OP_NOT = "!", OP_AND = "&&", OP_OR = "||";
	
	private final String operator;
	
	private List<IExternalFunctionExpression<VariableBinder>> operands = new ArrayList<IExternalFunctionExpression<VariableBinder>>();
	
	public ExternalFunctionCompoundExpression(String o){
		operator = o;
	}
	
	public void addOperand(IExternalFunctionExpression<VariableBinder> op){
		operands.add(op);
	}
	
	@Override
	public void setDataObject(VariableBinder data) {
		for(IExternalFunctionExpression<VariableBinder> operand : operands){
			operand.setDataObject(data);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws ExternalFunctionExpressionEvaluateException{
		
		
		IExternalFunctionExpression<VariableBinder> operand1 = operands.get(0);
		IExternalFunctionExpression<VariableBinder> operand2 = null;
			
		Class ret1 = operand1.getValueType();
		Class ret2 = null;
		
		if(operands.size() > 1){
			operand2 = operands.get(1);
			ret2 = operand2.getValueType();
		}
		
		Class retType = getOperandsType(ret1, ret2);
		
		Object value1 = operand1.getValue();
		Object value2 = null;
		
		if(operands.size() > 1){
			value2 = operand2.getValue();
		}
		
		
		try{
			if(operator.equals(OP_EQ)){
				if(retType == null){
					//TODO not use null type
					return value1.equals(value2);
				}
				else{
					return retType.cast(value1).equals(retType.cast(value2));
				}
			}
			else if(operator.equals(OP_NE)){
				if(retType == null){
					//TODO not use null type
					return value1.equals(value2);
				}
				else{
					
					return !retType.cast(value1).equals(retType.cast(value2));
					
				}	
			}
			else if(operator.equals(OP_GT)){
				if(retType == null){
					//TODO
					return false;
				}	
				else{
					if(retType.getCanonicalName().equals("java.lang.Integer")){
						return (Integer.valueOf(value1.toString()) > Integer.valueOf(value2.toString()));
					}
					else if(retType.getCanonicalName().equals("java.lang.Double")){
						return (Double.valueOf(value1.toString()) > Double.valueOf(value2.toString()));
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression "+retType);
					}
				}
			}
			else if(operator.equals(OP_LT)){
				if(retType == null){
					//TODO
					return false;
				}
				else{
					if(retType.getCanonicalName().equals("java.lang.Integer")){
						return (Integer.valueOf(value1.toString()) < Integer.valueOf(value2.toString()));
					}
					else if(retType.getCanonicalName().equals("java.lang.Double")){
						return (Double.valueOf(value1.toString()) < Double.valueOf(value2.toString()));
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression");
					}	
				}
			}
			else if(operator.equals(OP_GE)){
				if(retType == null){
					//TODO
					return false;
				}
				else{
					if(retType.getCanonicalName().equals("java.lang.Integer")){
						return (Integer.valueOf(value1.toString()) >= Integer.valueOf(value2.toString()));
					}
					else if(retType.getCanonicalName().equals("java.lang.Double")){
						return (Double.valueOf(value1.toString()) >= Double.valueOf(value2.toString()));
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression");
					}
				}
			}
			else if(operator.equals(OP_LE)){
				if(retType == null){
					//TODO
					return false;
				}
				else{
					if(retType.getCanonicalName().equals("java.lang.Integer")){
						return (Integer.valueOf(value1.toString()) <= Integer.valueOf(value2.toString()));
					}
					else if(retType.getCanonicalName().equals("java.lang.Double")){
						return (Double.valueOf(value1.toString()) <= Double.valueOf(value2.toString()));
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression");
					}	
				}
			}
			else if(operator.equals(OP_NOT)){
				if(retType == null){
					//TODO
					return false;
				}
				else{
					if(retType.getCanonicalName().equals("java.lang.Boolean")){
						return (!(boolean)value1);
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression");
					}	
				}
			}
			else if(operator.equals(OP_AND)){
				if(retType == null){
					//TODO
					return false;
				}
				else{
					if(retType.getCanonicalName().equals("java.lang.Boolean")){
						return ((boolean)value1 && (boolean)value2);
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression");
					}	
				}
			}
			else if(operator.equals(OP_OR)){
				if(retType == null){
					//TODO
					return false;
				}
				else{
					if(retType.getCanonicalName().equals("java.lang.Boolean")){
						return ((boolean)value1 || (boolean)value2);
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression");
					}	
				}
			}
			else{
				throw new ExternalFunctionExpressionEvaluateException("Not supported operator "+operator+" in external function expression");	
			}
		}
		catch(ClassCastException e){
			e.printStackTrace();
			throw new ExternalFunctionExpressionEvaluateException("Return value cast exception during evaluation external function expression");
		}
		
	}

	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValueType()
	 */
	@Override
	public Class getValueType() {
		
		return Boolean.class;
	
	}
	
	private Class getOperandsType(Class t1, Class t2) throws ExternalFunctionExpressionEvaluateException{
		if(t1 != null){
			String ts1 = t1.getCanonicalName();
			if(t2 != null){
				String ts2 = t2.getCanonicalName();
				if(ts1.equals(ts2)){
					return t1;
				}
				else{
					if((ts1.equals("java.lang.Double") && ts2.equals("java.lang.Integer")) || (ts1.equals("java.lang.Integer") && ts2.equals("java.lang.Double"))){
						return Double.class;
					}
					else{
						throw new ExternalFunctionExpressionEvaluateException("Different operand types during evaluating external function expression");
					}
				}
			}
			else{
				return t1;
			}
		}
		else{
			
			if(t2 != null){
				return t2;
			}
			else{
				return null;
			}
		}
	}
}
