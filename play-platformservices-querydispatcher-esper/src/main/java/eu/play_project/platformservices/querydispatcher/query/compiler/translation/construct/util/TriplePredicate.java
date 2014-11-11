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
public class TriplePredicate {
	
	private final int type;
	
	// content must at least have one element
	private List<String> content = new ArrayList<String>();
	
	private List<TripleObject> objects = new ArrayList<TripleObject>();
	
	public TriplePredicate(int t){
		type = t;
	}
	
	public int getType(){
		return type;
	}
	
	public List<TripleObject> getObjects(){
		return this.objects;
	}
	
	public List<String> getContent(){
		return this.content;
	}
}
