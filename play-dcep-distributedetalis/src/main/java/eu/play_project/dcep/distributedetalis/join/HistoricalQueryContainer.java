package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;

import eu.play_project.dcep.distributedetalis.api.VariableBindings;

/**
 * @author Ningyuan Pan
 * @author Roland Stühmer
 */
public class HistoricalQueryContainer {
	final static String SELECT = "SELECT";
	final static String WHERE = "WHERE";
	final static String VALUES = "VALUES";
	
	private final List<String> vvariables = new ArrayList<String>();
	private final Map<String, List<Object>> map;
	private String query;
	
	private final Logger logger = LoggerFactory.getLogger(HistoricalQueryContainer.class);
	
	public HistoricalQueryContainer(String query, VariableBindings variableBindings){
		if(query == null)
			throw new IllegalArgumentException("Original query must not be null");
		map = new VariableBindings();
		for (String varName : variableBindings.keySet()) {
			// Transfer only variables with nonempty bindings:
			if (variableBindings.get(varName) != null && !variableBindings.get(varName).isEmpty()) {
				map.put(varName, variableBindings.get(varName));
			}
		}
		if(!map.isEmpty())
			this.query = addVALUES(query);
		else
			this.query = query;
	}
	
	public String getQuery(){
		return query;
	}
	
	/**
	 * Add VALUES block into queries
	 */
	private String addVALUES(String oquery){
		int count = 0, index = 0;
		StringBuilder sparqlb = new StringBuilder(oquery);
		index = oquery.indexOf(VALUES);
		if(index != -1){
			throw new IllegalArgumentException("Original query already has " + VALUES + " clause");
		}
		else {
			index = oquery.indexOf(WHERE);
			logger.debug("where index: {}", index);
		
			// add VALUES block in WHERE block
			if (index == -1){
				throw new IllegalArgumentException("Original query has no " + WHERE + " clause");
			}
			else{
				while(index < oquery.length()){
					if(oquery.charAt(index) == '{'){
						count++;
					}
					else if(oquery.charAt(index) == '}'){
						count--;
						if(count == 0){
							break;
						}
					}
					index++;
				}
				String vb = makeVALUES();
				sparqlb.insert(index, vb);
			}
		}
		return sparqlb.toString();
	}
	
	/**
	 * Make VALUES block using variables and its values
	 */
	private String makeVALUES(){
		
		StringBuilder ret = new StringBuilder();
		if(makeVariableList()){
			ret.append("\n " + VALUES + " ( ");
			for(int i = 0; i < vvariables.size(); i++){
				ret.append("?");
				ret.append(vvariables.get(i));
				ret.append(" ");
			}
			ret.append(") {");
			
			ret = makeBody(ret, null, 0);
			
			ret.append("\n }\n");
		}
		return ret.toString();
	}
	
	private boolean makeVariableList(){
		boolean ret = false;
		for(String variable : map.keySet()){
			logger.debug("Add variable to list: {}", variable);
			vvariables.add(variable);
			ret = true;
		}
		return ret;
	}
	
	/**
	 * Make VALUES body of all combinations of values
	 */
	private StringBuilder makeBody(StringBuilder ret, StringBuilder p, int depth){
		StringBuilder path = p;
		String pathMinusOne;
		
		if(depth == vvariables.size()){
			path.append(")");
			ret.append(path);
		}
		else if(depth == 0){
			path = new StringBuilder();
			List<Object> values = map.get(vvariables.get(depth));
			if(values == null || values.isEmpty()){
				path.append("\n  ( UNDEF ");
				ret = makeBody(ret, path, depth+1);
			}
			else{
				for(int i = 0; i < values.size(); i++){
					path.delete(0, path.length());
					path.append("\n  ( ");
					path.append(makeVal(values.get(i)));
					path.append(" ");
					ret = makeBody(ret, path, depth+1);
				}
			}
		}
		else{
			pathMinusOne = path.toString();
			logger.debug(vvariables.get(depth));
			logger.debug("{}", map.get(vvariables.get(depth)));
			List<Object> values = map.get(vvariables.get(depth));
			if(values == null || values.isEmpty()){
				path.append("UNDEF ");
				ret = makeBody(ret, path, depth+1);
			}
			else{
				for(int i = 0; i < values.size(); i++){
					path.delete(0, path.length());
					path.append(pathMinusOne);
					path.append(makeVal(values.get(i)) + " ");
					ret = makeBody(ret, path, depth+1);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Covert a single value to String according to the value type.
	 */
	private String makeVal(Object value) {
		if (value instanceof String) {
			return "\"" + value + "\"";
		}
		else if (value instanceof Node_URI) {
			return "<" + value.toString() + ">";
		}
		else if (value instanceof Node) {
			return ((Node)value).toString(true);
		}
		else {
			return value.toString();
		}
	}

}
