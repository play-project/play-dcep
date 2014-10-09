/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;

/**
 * @author ningyuan 
 * 
 * Sep 27, 2014
 *
 */
public class ArrayMakerFactory {
	
	static IArrayMaker getArrayMaker(String t){
		if(t.equalsIgnoreCase("default")){
			return new DefaultArrayMaker();
		}
		else{
			return null;
		}
	}
}
