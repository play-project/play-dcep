/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ningyuan 
 * 
 * Oct 17, 2014
 *
 */
public class TripleObject {
	
	private final int type;
	
	// content must at least have one element
	private List<String> content = new ArrayList<String>();
	
	public TripleObject(int t){
		type = t;
	}
	
	public int getType(){
		return type;
	}
	
	public List<String> getContent(){
		return content;
	}
}
