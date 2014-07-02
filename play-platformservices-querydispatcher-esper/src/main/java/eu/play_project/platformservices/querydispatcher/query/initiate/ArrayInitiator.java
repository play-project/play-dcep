/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.initiate;


import eu.play_project.platformservices.bdpl.parser.util.ArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.ArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.initiate.util.InitiateException;


/**
 * @author ningyuan 
 * 
 * Jul 1, 2014
 *
 */
public class ArrayInitiator {
	
	
	
	public ArrayInitiator(){
		
	}
	
	public void initiate(ArrayTable arrayTable) throws InitiateException{
		for(String key : arrayTable.keySet()){
			ArrayTableEntry entry = arrayTable.get(key);
			
			switch(entry.getType()){
				case STATIC_EXPLICITE:{
					break;
				}
				case STATIC_QUERY:{
					break;
				}
				case DYNAMIC_VAR:{
					break;
				}
				case DYNAMIC_QUERY:{
					break;
				}
				default:{
					throw new InitiateException("Unsupported BDPL array type.");
				}
			}
		}
	}
}
