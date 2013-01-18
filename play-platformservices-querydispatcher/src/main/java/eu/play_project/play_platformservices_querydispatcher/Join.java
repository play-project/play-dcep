package eu.play_project.play_platformservices_querydispatcher;

import java.util.List;
import java.util.Map;

public class Join {
	
	public void join(Map<String, List<Variable>> r, Map<String, List<Variable>> s){
		// Hash-Join (Simple-Hash)
		
		// List all Elements from hashmap
		for(String key:r.keySet()){
			for (Variable varFromR : r.get(key)) {
				// test if in s exist the same key
				if(s.containsKey(key)){
					// test if the same variable exits
					for (Variable variableFormS : s.get(key)) {
						// Add values from s to r. If the same variable exists.
						if(varFromR.getName().equals(variableFormS.getName())){
							for (String value: variableFormS.getValues()) {
								varFromR.addValue(value);
							}
						}
					}
				}
				
			}
		}
	
	}

}
