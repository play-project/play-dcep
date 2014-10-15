/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;


import java.util.ArrayList;
import java.util.List;



import eu.play_project.platformservices.bdpl.parser.util.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTableEntry;
import eu.play_project.platformservices.querydispatcher.query.sparql.ISparqlRepository;
import eu.play_project.platformservices.querydispatcher.query.sparql.repository.TestRepository;

/**
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public class DefaultArrayMaker implements IArrayMaker {

	@Override
	public void make(BDPLArrayTableEntry entry, SubQueryTable subQueryTable) throws InitiateException {
		switch(entry.getType()){
			case STATIC_EXPLICITE:{
				make1(entry);
				break;
			}
			case STATIC_QUERY:{
				make2(entry);
				break;
			}
			case DYNAMIC_VAR:{
				make3(entry, subQueryTable);
				break;
			}
			case DYNAMIC_VAR_1:{
				make4(entry, subQueryTable);
				break;
			}
			case DYNAMIC_QUERY:{
				break;
			}
		}
	}
	
	/*
	 * STATIC_EXPLICITE
	 */
	private void make1(BDPLArrayTableEntry entry) throws InitiateException{
		entry.setArray(getElementsOfStaticArray(entry.getSource()));
	}
	
	/*
	 * Parse static array declaration, and initiate BDPL array.
	 * 
	 * Each dimension of array element must be grammar correct RDF literal or URI,
	 * otherwise the function will not work correctly.
	 */
	private BDPLArray getElementsOfStaticArray(String source) throws InitiateException{
		if(source == null || source.length() == 0){
			throw new IllegalArgumentException();
		}
		
		BDPLArray ret;
		
		char c;
		int state = 0, dimension = 0;
		StringBuffer oneDim = new StringBuffer();
		
		String [] value = null;
		List<String[]> element = new ArrayList<String[]>();
		List<List<String[]>> content = new ArrayList<List<String[]>>();
		
		for(int i = 0; i < source.length(); i++){
			c = source.charAt(i);
			
			switch(state){
				// start of element
				case 0:{
					if(c == ' '){
						continue;
					}
					else if(c == '\"'){
						oneDim.append(c);
						state = 1;
					}
					else if(c == '\''){
						oneDim.append(c);
						state = 4;
					}
					else if(c == ';'){
						throw new InitiateException("Empty element in static array declaration.");
					}
					else{
						oneDim.append(c);
						state = 2;
					}
					break;
				}
				// in one dimension (in label)
				case 1:{
					if(c == ' '){
						oneDim.append(c);
					}
					else if(c == '\"'){
						oneDim.append(c);
						
						value = new String[2];
						String s = oneDim.toString();
						
						if(s.length() > 2){
							value[0] = s.substring(1, s.length()-1);
						}
						else{
							value[0] = "";
						}
						
						state = 2;
					}
					else{
						oneDim.append(c);
					}
					break;
				}
				// in one dimension (after label)
				case 2:{
					if(c == ' '){
						if(value == null){
							value = new String[2];
							value[0] = oneDim.toString();
						}
						
						value[1] = oneDim.toString();
						
						element.add(value);
						oneDim.delete(0, oneDim.length());
						value = null;
						
						state = 3;
					}
					else if(c == ';'){
						if(value == null){
							value = new String[2];
							value[0] = oneDim.toString();
						}
						
						value[1] = oneDim.toString();
						
						element.add(value);
						oneDim.delete(0, oneDim.length());
						value = null;
						
						
						if(dimension != 0){
							if(dimension != element.size()){
								throw new InitiateException("Not matched dimension of static array in declaration. ");
							}
						}
						else{
							dimension = element.size();
						}
						
						content.add(element);
						element = new ArrayList<String[]>();
						
						state = 0;
					}
					else{
						oneDim.append(c);
					}
					break;
				}
				// after one dimension 
				case 3:{
					if(c == ' '){
						continue;
					}
					else if(c == '\"'){
						oneDim.append(c);
						state = 1;
					}
					else if(c == '\''){
						oneDim.append(c);
						state = 4;
					}
					else if(c == ';'){
						if(dimension != 0){
							if(dimension != element.size()){
								throw new InitiateException("Not matched dimension of static array in declaration. ");
							}
						}
						else{
							dimension = element.size();
						}
						
						content.add(element);
						element = new ArrayList<String[]>();
						
						state = 0;
					}
					else{
						oneDim.append(c);
						state = 2;
					}
					break;
				}
				// in one dimension (in label)
				case 4:{
					if(c == ' '){
						oneDim.append(c);
					}
					else if(c == '\''){
						oneDim.append(c);
						
						value = new String[2];
						String s = oneDim.toString();
						
						if(s.length() > 2){
							value[0] = s.substring(1, s.length()-1);
						}
						else{
							value[0] = "";
						}
						
						state = 2;
					}
					else{
						oneDim.append(c);
					}
					break;
				}
			}
		}
		
		
		if(oneDim.length() > 0){
			// label not end
			if(state == 1 || state == 4){
				throw new InitiateException("Illegal RDF literal in static array declaration.");
			}
			else{
				if(value == null){
					value = new String[2];
					value[0] = oneDim.toString();
				}
				
				value[1] = oneDim.toString();
				
				element.add(value);
				oneDim.delete(0, oneDim.length());
				value = null;

			}
		}
		
		if(element.size() > 0){
			if(dimension != 0){
				if(dimension != element.size()){
					throw new InitiateException("Not matched dimension of static array in declaration. ");
				}
			}
			else{
				dimension = element.size();
			}
			
			content.add(element);
		}
		
		if(dimension == 0){
			throw new InitiateException("A declared static array is empty. ");
		}
		
		String [][][] sContent = new String[content.size()][][];
		for(int i = 0; i < content.size(); i++){
			String [][] sElement = new String[content.get(i).size()][2];
			content.get(i).toArray(sElement);
			
			sContent[i] = sElement;
		}
		ret = new BDPLArray(sContent);
		ret.setDimension(dimension);
		
		return ret;
	}
	
	/*
	 * STATIC_QUERY
	 */
	private void make2(BDPLArrayTableEntry entry) throws InitiateException{
		
		ISparqlRepository repo = new TestRepository();
		repo.start();
		
		String[][][] contents = repo.query(entry.getSource());
		if(contents == null){
			throw new InitiateException("A static array could not be initiated by sparql query: "+entry.getSource());
		}
		
		BDPLArray array = new BDPLArray(contents);
		if(contents.length > 0){
			array.setDimension(contents[0].length);
		}
		else{
			throw new InitiateException("A static array initiated by sparql query: "+entry.getSource()+" is empty.");
		}
		
		entry.setArray(array);
		
		repo.close();
	}
	
	/*
	 * DYNAMIC_VAR
	 */
	private void make3(BDPLArrayTableEntry entry, SubQueryTable subQueryTable) throws InitiateException{
		BDPLArray array = entry.getArray();
		
		if(array == null){
			throw new InitiateException("A dynamic array is not created before initiation.");
		}
		else{
			SubQueryTableEntry qEntry = new SubQueryTableEntry();
			String[] svs = getSelectedVarsOfDynamicArray(entry.getSource());
			if(svs.length == 0){
				throw new InitiateException("A synchronized dynamic array selects no variable from this query.");
			}
			array.setDimension(svs.length);
			qEntry.setArray(array);
			qEntry.setSelectedVars(svs);
			subQueryTable.add(qEntry);
		}
	}
	
	/*
	 * @return array of names of selected variables. never be null
	 */
	private String[] getSelectedVarsOfDynamicArray(String source){
		
		List<String> tempVars = new ArrayList<String>();
		StringBuffer var = new StringBuffer();
		
		char c;
		int state = 0;
		for(int i = 0; i < source.length(); i++){
			c = source.charAt(i);
			
			switch(state){
				// start of var
				case 0:{
					if(c == ' '){
						continue;
					}
					else{
						var.append(c);
						state = 1;
					}
					break;
				}
				// middle of var
				case 1:{
					if(c == ' '){
						tempVars.add(var.toString());
						var.delete(0, var.length());
						state = 0;
					}
					else{
						var.append(c);
					}
					break;
				}
			}
		}
		
		if(var.length() > 0){
			tempVars.add(var.toString());
		}
			
		String [] ret = new String[tempVars.size()];
		tempVars.toArray(ret);
		
		return ret;
	}
	
	/*
	 * DYNAMIC_VAR_1
	 */
	private void make4(BDPLArrayTableEntry entry, SubQueryTable subQueryTable) throws InitiateException{
		BDPLArray array = entry.getArray();
		
		if(array == null){
			throw new InitiateException("A dynamic array is not created before initiation.");
		}
		else{
			SubQueryTableEntry qEntry = new SubQueryTableEntry();
			String source = entry.getSource();
			int i = getStartIndexOftoArrayMethod(source);
			if(i >= source.length()){
				throw new InitiateException("A synchronized dynamic array selects no variable from this query.");
			}
			else{
				String fn = source.substring(0, i).trim();
				String[] svs = getSelectedVarsOfDynamicArray(source.substring(i, source.length()));
				if(svs.length == 0){
					throw new InitiateException("A synchronized dynamic array selects no variable from this query.");
				}
				array.setDimension(svs.length);
				qEntry.setArray(array);
				qEntry.setToArrayMethod(fn);
				qEntry.setSelectedVars(svs);
				subQueryTable.add(qEntry);
			}
			
		}
	}
	
	private int getStartIndexOftoArrayMethod(String source){
		
		char c;
		int state = 0;
		int ret = 0;
		for(ret = 0; ret < source.length(); ret++){
			c = source.charAt(ret);
			
			switch(state){
				// start of method
				case 0:{
					if(c == ' '){
						continue;
					}
					else{
						state = 1;
					}
					break;
				}
				// middle of var
				case 1:{
					if(c == ' '){
						return ret;
					}
					else{
						continue;
					}
				}
			}
		}
		
		return ret;
	}
}
