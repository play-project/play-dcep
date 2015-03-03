/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;


import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;


/**
 * The class of array initiator which reads the array table and creates
 * a sub-query table of a bdpl query. After the initiation, the array 
 * table is filled with actual array objects created by an array maker. 
 * The sub-query table records other bdpl queries that update arrays from 
 * this bdpl query.
 * 
 * 
 * 
 * @author ningyuan 
 * 
 * Jul 1, 2014
 *
 */
public class ArrayInitiator {
	
	// the array maker
	private final IArrayMaker arrayMaker;
	
	/**
	 * create the object with a specific type of array maker
	 * 
	 * @param type
	 */
	public ArrayInitiator(String type){
		arrayMaker = ArrayMakerFactory.getArrayMaker(type);
	}
	
	
	/**
	 * initiate the sub-query table and create actual array objects in the array table
	 * 
	 * @param arrayTable
	 * @return
	 * @throws InitiateException
	 */
	public SubQueryTable initiate(BDPLArrayTable arrayTable) throws InitiateException{
		SubQueryTable subQueryTable = new SubQueryTable();
		
		for(String key : arrayTable.keySet()){
			BDPLArrayTableEntry entry = arrayTable.get(key);
			
			arrayMaker.make(entry, subQueryTable);
			
		}
		
		return subQueryTable;
	}
}
