/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.util;



/**
 * @author ningyuan 
 * 
 * Oct 15, 2014
 *
 */
public interface IFunction {
	
	public Object invoke(Object ... parameters) throws FunctionInvocationException;
	
	public boolean compareParameterTypes(Class[] pts);
	
	public Class[] getParameterTypes();
	
	public Class getReturnType();
	
	public String getClassName();
}
