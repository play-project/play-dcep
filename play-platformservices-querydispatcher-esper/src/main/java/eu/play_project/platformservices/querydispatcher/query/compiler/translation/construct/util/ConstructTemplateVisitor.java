/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util;

/**
 * The interface of a construct template visitor. A construct template visitor
 * can visit the construct template. Different visitor algorithms
 * for different template structures.
 * 
 * 
 * @author ningyuan 
 * 
 * Oct 21, 2014
 *
 */
public abstract class ConstructTemplateVisitor {
	
	/**
	 * visit the construct template passed by parameter.
	 * 
	 * @param template
	 */
	public abstract void visit(ConstructTemplate template);
}
