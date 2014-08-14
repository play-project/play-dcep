/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import java.util.List;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.ExFunction;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.ExFunctionInvocationException;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.ExFunctionTable;

/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class ExternalFunctionFunctionExpression implements IExternalFunctionExpression {
	
	public static final String PARA_TYPE_INT = "int", PARA_TYPE_DECIMAL = "decimal", PARA_TYPE_BOOLEAN = "boolean", 
			PARA_TYPE_STR = "str", PARA_TYPE_VAR = "var", PARA_TYPE_ARRAY = "array";
	
	private Class valueType;
	
	private final String fn;
	
	private final VariableBinder vb;
	
	private final ExFunctionTable ft = ExFunctionTable.getInstance();
	
	private List<String[]> paras;
	
	public ExternalFunctionFunctionExpression(String functionName, VariableBinder varBinder) throws ExternalFunctionExpressionEvaluateException{
		ExFunction ef = ft.getFunction(functionName);
		/*if(ef == null){
			throw new ExternalFunctionExpressionEvaluateException("External function \'"+functionName+"\' is not loaded into system");
		}
		
		valueType = ef.getReturnType();*/
		fn = functionName;
		vb = varBinder;
	}
	
	/**
	 * 
	 * @param paras (must not be null)
	 * @throws ExternalFunctionExpressionEvaluateException 
	 */
	public void setParameters(List<String[]> paras) throws ExternalFunctionExpressionEvaluateException{
		ExFunction ef = ft.getFunction(fn);
		if(ef == null){
			throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' is not loaded into system");
		}
		
		Class[] efpts = ef.getParameterTypes();
		
		if(paras.size() != efpts.length){
			throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' declared different parameter size as loaded function");
		}
		
		for(int i = 0; i < efpts.length; i++){
			Class efpt = efpts[i];
			String pt = paras.get(i)[0];
			
			if(efpt.isArray()){
				if(!pt.equalsIgnoreCase(PARA_TYPE_ARRAY)){
					throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' declared different parameter type as loaded function");
				}
			}
			else{
				String efptn = efpt.getCanonicalName();
				
				if(pt.equalsIgnoreCase(PARA_TYPE_INT)){
					if(!efptn.equals("java.lang.Integer") && !efptn.equals("int")){
						throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else if(pt.equalsIgnoreCase(PARA_TYPE_DECIMAL)){
					if(!efptn.equals("java.lang.Double") && !efptn.equals("double")){
						throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else if(pt.equalsIgnoreCase(PARA_TYPE_BOOLEAN)){
					if(!efptn.equals("java.lang.Boolean") && !efptn.equals("boolean")){
						throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else if(pt.equalsIgnoreCase(PARA_TYPE_STR)){
					if(!efptn.equals("java.lang.String")){
						throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else{
					throw new ExternalFunctionExpressionEvaluateException("Not supported parameter type "+pt+" in external function");
				}
			}
		}
		
		this.paras = paras;
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws ExternalFunctionExpressionEvaluateException{
		ExFunction ef = ft.getFunction(fn);
		if(ef == null){
			throw new ExternalFunctionExpressionEvaluateException("External function \'"+fn+"\' is not loaded into system");
		}
		
		// paras must not be null. setParameters() must be called before
		Object [] pObjects = new Object[paras.size()]; 
		
		for(int i = 0; i < paras.size(); i++){
			String [] para = paras.get(i);
			String pt = para[0];
			if(pt.equalsIgnoreCase(PARA_TYPE_ARRAY)){
				String[][][] o = vb.getArray(para[1]);
				if(o != null){
					pObjects[i] = o;
				}
				else{
					throw new ExternalFunctionExpressionEvaluateException("Array variable ?"+para[1]+"() could not be found when evaluating external function");
				}
			}
			else if(pt.equalsIgnoreCase(PARA_TYPE_VAR)){
				String o = vb.getVar(para[1]);
				if(o != null){
					pObjects[i] = o;
				}
				else{
					throw new ExternalFunctionExpressionEvaluateException("Variable ?"+para[1]+" could not be binded when evaluating external function");
				}
			}
			else {
				pObjects[i] = para[1];
			}
		}
		
		Object ret;
		try {
			ret = ef.invoke(pObjects);
		} catch (ExFunctionInvocationException e) {
			throw new ExternalFunctionExpressionEvaluateException(e.getMessage());
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValueType()
	 */
	@Override
	public Class getValueType() {
		return valueType;
	}


}
