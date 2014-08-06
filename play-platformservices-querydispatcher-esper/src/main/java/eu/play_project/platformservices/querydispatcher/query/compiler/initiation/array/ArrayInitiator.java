/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;


import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;


/**
 * @author ningyuan 
 * 
 * Jul 1, 2014
 *
 */
public class ArrayInitiator {
	
	private final IArrayMaker arrayMaker;
	
	public ArrayInitiator(IArrayMaker m){
		arrayMaker = m;
	}
	
	public SubQueryTable initiate(BDPLArrayTable arrayTable) throws InitiateException{
		SubQueryTable subQueryTable = new SubQueryTable();
		
		for(String key : arrayTable.keySet()){
			BDPLArrayTableEntry entry = arrayTable.get(key);
			
			arrayMaker.make(entry, subQueryTable);
			
		}
		
		return subQueryTable;
	}
}
