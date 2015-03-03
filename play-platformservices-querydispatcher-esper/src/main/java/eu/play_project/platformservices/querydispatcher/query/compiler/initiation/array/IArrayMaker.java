/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;

/**
 * The interface of a array maker. An array maker is responsible for
 * create array objects used in a query. Because there could be different
 * types of array and different formats of array declarations in a query,
 * the interface defines only the common operations of an array maker. 
 * 
 * 
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public interface IArrayMaker {
	
	public void make(BDPLArrayTableEntry entry, SubQueryTable subQueryTable) throws InitiateException;
}
