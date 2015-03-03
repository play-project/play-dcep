/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;

/**
 * The factory class of concrete array makers.
 * 
 * 
 * @author ningyuan 
 * 
 * Sep 27, 2014
 *
 */
public class ArrayMakerFactory {
	
	public static final String TYPE_DEFAULT = "default";
	
	static IArrayMaker getArrayMaker(String t){
		//TODO 
		if(t.equalsIgnoreCase(TYPE_DEFAULT)){
			return new DefaultArrayMaker();
		}
		else{
			return null;
		}
	}
}
