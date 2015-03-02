/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The neutral representation of the subject of an RDF triple.
 * 
 * 
 * @author ningyuan 
 * 
 * Oct 17, 2014
 *
 */
public class TripleSubject {
	
	private final int type;
	
	/* the content of the subject. The content must at least have one element.
	 * 
	 * variable
	 * content[0]: variable name
	 * 
	 * 
	 * iri
	 * content[0]: iri
	 * 
	 */
	private List<String> content = new ArrayList<String>();
	
	private List<TriplePredicate> predicates = new ArrayList<TriplePredicate>();
	
	public TripleSubject(int t){
		type = t;
	}
	
	public int getType(){
		return type;
	}
	
	public List<TriplePredicate> getPredicates(){
		return predicates;
	}
	
	public List<String> getContent(){
		return content;
	}
}
