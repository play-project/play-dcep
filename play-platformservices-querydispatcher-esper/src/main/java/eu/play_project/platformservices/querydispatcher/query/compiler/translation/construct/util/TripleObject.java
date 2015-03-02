/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The neutral representation of the object of an RDF triple.
 * 
 * 
 * @author ningyuan 
 * 
 * Oct 17, 2014
 *
 */
public class TripleObject {
	
	private final int type;
	
	/* the content of the object. The content must at least have one element.
	 * 
	 * variable
	 * content[0]: variable name
	 * 
	 * 
	 * iri
	 * content[0]: iri
	 * 
	 * 
	 * array variable
	 * content[0]: function iri
	 * content[1]: array name
     * content[2]: array index
     * 
     * 
     * rdf literal
	 * content[0]: label
	 * content[1]: language tag
	 * content[0]: data type
	 */
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
