/**
 * 
 */
package eu.play_project.dcep.distribution.eventcloud.remotetests;

import java.util.List;
import java.util.Map;

/**
 * @author Ningyuan Pan
 *
 */
public class SPARQLParser {
	final String SELECT = "SELECT";
	final String WHERE = "WHERE";
	final String VALUES = "VALUES";
	
	private int index = 0;
	private int temp = -1;
	
	private Map<String, List<String>> map;
	
	public SPARQLParser(Map<String, List<String>> m){
		map = m;
	}
	
	public String addVALUESBlocks(String sparql){
		int count = 0;
		StringBuilder sparqlb = new StringBuilder(sparql);
		
		index = sparql.indexOf(WHERE);
			System.out.println("where index: "+index);
		
		// add vlues block in where block
		if(index != 0){
			while(index < sparql.length()){
				if(sparql.charAt(index) == '{'){
					count++;
						System.out.println("{ "+index);
				}
				else if(sparql.charAt(index) == '}'){
					count--;
					System.out.println("} "+index);
					if(count == 0){
						break;
					}
				}
				index++;
			}
			String vb = makeVALUESBlocks(map);
			System.out.println("value blocks: "+vb);
			sparqlb.insert(index, vb);
				System.out.println(sparqlb.toString());
		}
		return sparqlb.toString();
	}
	
	public String makeVALUESBlocks(Map<String, List<String>> map){
		StringBuilder ret = new StringBuilder();
		
		for(String variable : map.keySet()){
			ret.append(" "+VALUES+" ?"+variable+" {");
			for(String value : map.get(variable)){
				ret.append(" :"+value);
			}
			ret.append(" }");
		}
		return ret.toString();
	}
}
