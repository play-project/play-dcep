/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import java.util.List;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IFunctionExpression;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionInvocationException;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionTable;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.IFunction;

/**
 * A concrete external function expression. This expression represents a
 * simple function with its name and parameter declaration.
 * 
 * 
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class FunctionFunctionExpression implements IFunctionExpression<VariableBinder> {
	
	public static final String PARA_TYPE_INT = "int", PARA_TYPE_DECIMAL = "decimal", PARA_TYPE_BOOLEAN = "boolean", 
			PARA_TYPE_STR = "str", PARA_TYPE_VAR = "var", PARA_TYPE_ARRAY = "array";
	
	private VariableBinder vb;
	
	private Class valueType;
	
	private final String fn;
	
	private List<String[]> paras;
	
	private final FunctionTable ft = FunctionTable.getInstance();
	

	public FunctionFunctionExpression(String functionName) throws BDPLFilterException{
		IFunction ef = ft.getFunction(functionName);
		if(ef == null){
			throw new BDPLFilterException("External function \'"+functionName+"\' is not loaded into system");
		}
		
		valueType = ef.getReturnType();
		fn = functionName;
	}
	
	/*
	 * used for copy
	 */
	private FunctionFunctionExpression(String functionName, Class vt, List<String[]> paras){
		fn = functionName;
		valueType = vt;
		this.paras = paras;
	}
	
	@Override
	public IFunctionExpression<VariableBinder> copy(){
		return new FunctionFunctionExpression(fn, valueType, paras);
	}
	
	@Override
	public void setDataObject(VariableBinder data) {
		vb = data;
	}
	
	/**
	 * This method must be called before calling getValue()
	 * 
	 * @param paras (must not be null)
	 * @throws BDPLFilterException 
	 */
	public void setParameters(List<String[]> paras) throws BDPLFilterException{
		IFunction ef = ft.getFunction(fn);
		if(ef == null){
			throw new BDPLFilterException("External function \'"+fn+"\' is not loaded into system");
		}
		
		Class[] efpts = ef.getParameterTypes();
		
		if(paras.size() != efpts.length){
			throw new BDPLFilterException("External function \'"+fn+"\' declared different parameter size as loaded function");
		}
		
		for(int i = 0; i < efpts.length; i++){
			Class efpt = efpts[i];
			String pt = paras.get(i)[0];
			
			if(efpt.isArray()){
				if(!pt.equalsIgnoreCase(PARA_TYPE_ARRAY)){
					throw new BDPLFilterException("External function \'"+fn+"\' declared different parameter type as loaded function");
				}
			}
			else{
				String efptn = efpt.getCanonicalName();
				
				if(pt.equalsIgnoreCase(PARA_TYPE_INT)){
					if(!efptn.equals("java.lang.Integer") && !efptn.equals("int")){
						throw new BDPLFilterException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else if(pt.equalsIgnoreCase(PARA_TYPE_DECIMAL)){
					if(!efptn.equals("java.lang.Double") && !efptn.equals("double")){
						throw new BDPLFilterException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else if(pt.equalsIgnoreCase(PARA_TYPE_BOOLEAN)){
					if(!efptn.equals("java.lang.Boolean") && !efptn.equals("boolean")){
						throw new BDPLFilterException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else if(pt.equalsIgnoreCase(PARA_TYPE_STR)){
					if(!efptn.equals("java.lang.String")){
						throw new BDPLFilterException("External function \'"+fn+"\' declared different parameter type as loaded function");
					}
				}
				else{
					throw new BDPLFilterException("Not supported parameter type "+pt+" in external function");
				}
			}
		}
		
		this.paras = paras;
	}
	
	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws BDPLFilterException{
		IFunction ef = ft.getFunction(fn);
		if(ef == null){
			throw new BDPLFilterException("External function \'"+fn+"\' is not loaded into system");
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
					throw new BDPLFilterException("Array variable ?"+para[1]+"() could not be found when evaluating external function");
				}
			}
			else if(pt.equalsIgnoreCase(PARA_TYPE_VAR)){
				String o = vb.getVarValue(para[1]);
				if(o != null){
					pObjects[i] = o;
				}
				else{
					throw new BDPLFilterException("Variable ?"+para[1]+" could not be binded when evaluating external function");
				}
			}
			else {
				pObjects[i] = para[1];
			}
		}
		
		Object ret;
		try {
			ret = ef.invoke(pObjects);
		} catch (FunctionInvocationException e) {
			throw new BDPLFilterException(e.getMessage());
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
