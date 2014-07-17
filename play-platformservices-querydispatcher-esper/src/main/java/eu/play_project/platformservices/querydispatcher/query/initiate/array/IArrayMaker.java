/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.initiate.array;

import eu.play_project.platformservices.bdpl.parser.util.ArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.initiate.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.initiate.util.SubQueryTable;

/**
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public interface IArrayMaker {
	
	public void make(ArrayTableEntry entry, SubQueryTable subQueryTable) throws InitiateException;
}
