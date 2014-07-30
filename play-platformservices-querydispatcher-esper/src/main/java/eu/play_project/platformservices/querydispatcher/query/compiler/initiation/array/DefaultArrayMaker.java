/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array;


import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
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
	 */
	private BDPLArray getElementsOfStaticArray(String source) throws InitiateException{
		if(source == null || source.length() == 0){
			throw new IllegalArgumentException();
		}
		
		BDPLArray ret;
		
		char c;
		int state = 0, dimension = 0;
		StringBuffer oneDim = new StringBuffer();
	
		List<String> element = new ArrayList<String>();
		List<List<String>> content = new ArrayList<List<String>>();
		for(int i = 0; i < source.length(); i++){
			c = source.charAt(i);
			
			switch(state){
				// start of element
				case 0:{
					if(c == ' '){
						continue;
					}
					else if(c == ';'){
						throw new InitiateException("No element defined in static array.");
					}
					else{
						oneDim.append(c);
						state = 1;
					}
					break;
				}
				// in one dimension
				case 1:{
					if(c == ' '){
						state = 2;
						element.add(oneDim.toString());
						oneDim.delete(0, oneDim.length());
					}
					else if(c == ';'){
						element.add(oneDim.toString());
						oneDim.delete(0, oneDim.length());
						
						if(dimension != 0){
							if(dimension != element.size()){
								throw new InitiateException("Not matched dimension of static array in declaration. ");
							}
						}
						else{
							dimension = element.size();
						}
						
						content.add(element);
						element = new ArrayList<String>();
						
						state = 0;
					}
					else{
						oneDim.append(c);
					}
					break;
				}
				// after one dimension
				case 2:{
					if(c == ' '){
						continue;
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
						element = new ArrayList<String>();
						
						state = 0;
					}
					else{
						oneDim.append(c);
						state = 1;
					}
					break;
				}
			}
		}
		
		
		if(oneDim.length() > 0){
			element.add(oneDim.toString());
			oneDim.delete(0, oneDim.length());
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
		
		String [][] sContent = new String[content.size()][];
		for(int i = 0; i < content.size(); i++){
			String [] sElement = new String[content.get(i).size()];
			content.get(i).toArray(sElement);
			
			sContent[i] = sElement;
		}
		ret = new BDPLArray(sContent);
		return ret;
	}
	
	/*
	 * STATIC_QUERY
	 */
	private void make2(BDPLArrayTableEntry entry) throws InitiateException{
		
		/*List<String> varNames = null;
		
		try{
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(entry.getSource());
			StringEscapesProcessor.process(qc);
			BaseDeclProcessor.process(qc, null);
			WildcardProjectionProcessor.process(qc);
			BlankNodeVarProcessor.process(qc);
			varNames = ArrayElementProcessor.process(qc);
		}
		
		catch (TokenMgrError e) {
			throw new InitiateException(e.getMessage());
		}
		catch(MalformedQueryException e){
			throw new InitiateException(e.getMessage());
		}
		catch (ParseException e) {
			throw new InitiateException(e.getMessage());
		}*/
		
		
		
		ISparqlRepository repo = new TestRepository();
		repo.start();
		
		entry.setArray(new BDPLArray(repo.query(entry.getSource())));
		
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
			qEntry.setArray(array);
			qEntry.setSelectedVars(getSelectedVarsOfDynamicArray(entry.getSource()));
			subQueryTable.add(qEntry);
		}
	}
	
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
}
