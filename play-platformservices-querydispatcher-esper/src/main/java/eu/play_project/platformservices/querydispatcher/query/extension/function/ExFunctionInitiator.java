/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function;

import java.lang.reflect.Method;

import eu.play_project.platformservices.querydispatcher.query.extension.function.implement.DefaultExFunction;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.ExternalFunctionTable;

/**
 * @author ningyuan 
 * 
 * Aug 6, 2014
 *
 */
public class ExFunctionInitiator {
	
	public static void initiate(ExternalFunctionTable fTable){
		
		Class<DefaultExFunction> fc = DefaultExFunction.class;
		
		Method[] methods = fc.getDeclaredMethods();
		
		for(int i = 0; i < methods.length; i++){
			ExFunction ef = new ExFunction(methods[i].getName(), methods[i].getParameterTypes()); 
			fTable.putFunctionClass(ef, fc);
		}
	}
}
