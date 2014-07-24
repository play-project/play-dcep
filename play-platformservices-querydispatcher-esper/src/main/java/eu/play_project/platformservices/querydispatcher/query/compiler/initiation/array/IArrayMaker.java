/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;

import eu.play_project.platformservices.bdpl.parser.util.ArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;

/**
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public interface IArrayMaker {
	
	public void make(ArrayTableEntry entry, SubQueryTable subQueryTable) throws InitiateException;
}
