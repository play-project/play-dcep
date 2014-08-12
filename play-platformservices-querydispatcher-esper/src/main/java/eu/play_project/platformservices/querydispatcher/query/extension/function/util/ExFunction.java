/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ningyuan 
 * 
 * Aug 8, 2014
 *
 */
public class ExFunction {
	public static final String FUN_NAME = "execute";
	
	private final String className;
	
	private final Class exfunction;
	
	private final Class[] paraTypes;
	
	private final Class retType;
	
	private int[] castTypes;
	
	public ExFunction(String cn, Class ef, Class[] pts, Class r) throws ExFunctionParameterTypeException{
		className = cn;
		exfunction = ef;
		paraTypes = pts;
		makeCastType(pts);
		retType = r;
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
	 * 
	 * 
	 * @param parameters parameters from bdpl query. pass String[][][] if it is an array
	 * 		  pass String if it is an simple variable (must not be null)
	 * 
	 * @return
	 * @throws ExFunctionInvocationException
	 */
	public Object invoke(Object ... parameters) throws ExFunctionInvocationException{
		if(parameters.length != paraTypes.length){
			throw new ExFunctionInvocationException("Function could not be invocated because of different size of parameters");
		}
		
		try {
			Object[] paras = new Object[paraTypes.length];
			for(int i = 0; i < castTypes.length; i++){
				paras[i] = ExFunctionParameterCastor.cast(castTypes[i], parameters[i]);
			}
			
			Method m = exfunction.getMethod(FUN_NAME, paraTypes);
			
			return m.invoke(exfunction.newInstance(), paras);
		
		}catch (ExFunctionParameterCastException e) {
			throw new ExFunctionInvocationException(e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new ExFunctionInvocationException(e.getMessage());
		} catch (SecurityException e) {
			throw new ExFunctionInvocationException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ExFunctionInvocationException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ExFunctionInvocationException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new ExFunctionInvocationException(e.getMessage());
		} catch (InstantiationException e) {
			throw new ExFunctionInvocationException(e.getMessage());
		} 
	
	}
	
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
	
	String getClassName(){
		return className;
	}
	
	private void makeCastType(Class[] pts) throws ExFunctionParameterTypeException{
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
					castTypes[i] = ExFunctionParameterCastor.TYPE_ARRAY_DOUBLE;
				}
				else if(typeName.equals("java.lang.Integer") || typeName.equals("int")){
					castTypes[i] = ExFunctionParameterCastor.TYPE_ARRAY_INT;
				}
				else if(typeName.equals("java.lang.String")){
					castTypes[i] = ExFunctionParameterCastor.TYPE_ARRAY_STR;
				}
				else{
					throw new ExFunctionParameterTypeException("Unsupported parameter type "+typeName+"[]");
				}
				
			}
			else{
				
				if(typeName.equals("java.lang.Double") || typeName.equals("double")){
					castTypes[i] = ExFunctionParameterCastor.TYPE_DOUBLE;
				}
				else if(typeName.equals("java.lang.Integer") || typeName.equals("int")){
					castTypes[i] = ExFunctionParameterCastor.TYPE_INT;
				}
				else if(typeName.equals("java.lang.String")){
					castTypes[i] = ExFunctionParameterCastor.TYPE_STR;
				}
				else if(typeName.equals("java.lang.Boolean") || typeName.equals("boolean")){
					castTypes[i] = ExFunctionParameterCastor.TYPE_BOOLEAN;
				}
				else{
					throw new ExFunctionParameterTypeException("Unsupported parameter type "+typeName);
				}
			}
		}
	}
}
