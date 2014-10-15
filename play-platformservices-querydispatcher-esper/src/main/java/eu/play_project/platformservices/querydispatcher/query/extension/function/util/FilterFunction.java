/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * external function represented in program
 * 
 * @author ningyuan 
 * 
 * Aug 8, 2014
 *
 */
public class FilterFunction implements IFunction{
	/**
	 * the name of routine method in function class
	 */
	public static final String METHOD_NAME = "execute";
	
	/*
	 * canonical name of function class
	 */
	private final String className;
	
	/*
	 * class object of function
	 */
	private final Class exfunction;
	
	/*
	 * classes of parameter type
	 */
	private final Class[] paraTypes;
	
	/*
	 * class of return type
	 */
	private final Class retType;
	
	/*
	 * cast types used by caster when casting parameters from events for function calling
	 */
	private int[] castTypes;
	
	public FilterFunction(String cn, Class ef, Class[] pts, Class r) throws FunctionParameterTypeException{
		className = cn;
		exfunction = ef;
		
		makeCastType(pts);
		
		paraTypes = pts;
		
		retType = makeReturnType(r);
		
	}
	
	public Class getReturnType(){
		return retType;
	}
	
	/**
	 * do not change the content of return
	 * 
	 * @return
	 */
	public Class[] getParameterTypes(){
		return paraTypes;
	}
	
	/**
	 * invoke the actual function with parameters from events. If parameter is type array, 
	 * please pass an object String[length][dimension][2], if parameter is simple variable, 
	 * please pass an object String
	 * 
	 * 
	 * @param parameters parameters from events. pass a String[][][] if it is an array
	 * 		  pass a String if it is an simple variable (must not be null)
	 * 
	 * @return
	 * @throws FunctionInvocationException
	 */
	@Override
	public Object invoke(Object ... parameters) throws FunctionInvocationException{
		if(parameters.length != paraTypes.length){
			throw new FunctionInvocationException("Function could not be invocated because of different size of parameters");
		}
		
		try {
			Object[] paras = new Object[paraTypes.length];
			for(int i = 0; i < castTypes.length; i++){
				paras[i] = FilterFunctionParameterCastor.cast(castTypes[i], parameters[i]);
			}
			
			Method m = exfunction.getMethod(METHOD_NAME, paraTypes);
			
			return m.invoke(exfunction.newInstance(), paras);
		
		}catch (FunctionParameterCastException e) {
			throw new FunctionInvocationException(e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new FunctionInvocationException(e.getMessage());
		} catch (SecurityException e) {
			throw new FunctionInvocationException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new FunctionInvocationException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new FunctionInvocationException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new FunctionInvocationException(e.getMessage());
		} catch (InstantiationException e) {
			throw new FunctionInvocationException(e.getMessage());
		} 
	
	}
	
	/**
	 * check whether two parameter types are identical
	 * 
	 * @param pts
	 * @return
	 */
	@Override
	public boolean compareParameterTypes(Class[] pts){
		if(paraTypes.length == pts.length){
			for(int i = 0; i < paraTypes.length; i++){
				if(!paraTypes[i].getCanonicalName().equals(pts[i].getCanonicalName())){
					return false;
				}
			}
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public String getClassName(){
		return className;
	}
	
	/*
	 * make parameter class types to casting types.
	 * allowd parameter types are: int, double, String
	 */
	private void makeCastType(Class[] pts) throws FunctionParameterTypeException{
		castTypes = new int[pts.length];
		
		for(int i = 0; i < pts.length; i++){
			Class type = pts[i];
			boolean array = type.isArray();
			
			while(type.isArray()){
				type = type.getComponentType();
			}
			
			String typeName = type.getCanonicalName();
			
			if(array){
				
				if(typeName.equals("java.lang.Double") || typeName.equals("double")){
					castTypes[i] = FilterFunctionParameterCastor.TYPE_ARRAY_DOUBLE;
				}
				else if(typeName.equals("java.lang.Integer") || typeName.equals("int")){
					castTypes[i] = FilterFunctionParameterCastor.TYPE_ARRAY_INT;
				}
				else if(typeName.equals("java.lang.String")){
					castTypes[i] = FilterFunctionParameterCastor.TYPE_ARRAY_STR;
				}
				else{
					throw new FunctionParameterTypeException("Unsupported parameter type "+typeName+"[]");
				}
				
			}
			else{
				
				if(typeName.equals("java.lang.Double") || typeName.equals("double")){
					castTypes[i] = FilterFunctionParameterCastor.TYPE_DOUBLE;
				}
				else if(typeName.equals("java.lang.Integer") || typeName.equals("int")){
					castTypes[i] = FilterFunctionParameterCastor.TYPE_INT;
				}
				else if(typeName.equals("java.lang.String")){
					castTypes[i] = FilterFunctionParameterCastor.TYPE_STR;
				}
				else if(typeName.equals("java.lang.Boolean") || typeName.equals("boolean")){
					castTypes[i] = FilterFunctionParameterCastor.TYPE_BOOLEAN;
				}
				else{
					throw new FunctionParameterTypeException("Unsupported parameter type "+typeName);
				}
			}
		}
	}
	
	private Class makeReturnType(Class r) throws FunctionParameterTypeException{
		if(r.isPrimitive()){
			String retName = r.getCanonicalName();
			
			if(retName.equals("java.lang.Double") || retName.equals("double")){
				return Double.class;
			}
			else if(retName.equals("java.lang.Integer") || retName.equals("int")){
				return Integer.class;
			}
			else if(retName.equals("java.lang.Boolean") || retName.equals("boolean")){
				return Boolean.class;
			}
			else{
				throw new FunctionParameterTypeException("Unsupported return type "+retName);
			}
		}
		else{
			if(r.getCanonicalName().equals("java.lang.String")){
				return String.class;
			}
			else{
				throw new FunctionParameterTypeException("Unsupported return type "+r);
			}
		}
	}
}
