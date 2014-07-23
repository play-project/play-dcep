/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.initiate.array;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.array.BDPLArrayElement;
import eu.play_project.platformservices.bdpl.parser.util.ArrayTableEntry;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;
import eu.play_project.platformservices.querydispatcher.query.initiate.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.initiate.util.SubQueryTable;
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
	public void make(ArrayTableEntry entry, SubQueryTable subQueryTable) throws InitiateException {
		switch(entry.getType()){
			case STATIC_EXPLICITE:{
				make1(entry, subQueryTable);
				break;
			}
			case STATIC_QUERY:{
				make2(entry, subQueryTable);
				break;
			}
			case DYNAMIC_VAR:{
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
	private void make1(ArrayTableEntry entry, SubQueryTable table) throws InitiateException{
		entry.setArray(parseStaticArray(entry.getSource()));
	}
	
	/*
	 * Parse static array declaration, and initiate BDPL array.
	 */
	private BDPLArray parseStaticArray(String source) throws InitiateException{
		if(source == null || source.length() == 0){
			throw new IllegalArgumentException();
		}
		
		BDPLArray ret;
		BDPLArrayElement head = null, current = null, temp = null;
		
		char c;
		int state = 0, dimension = 0;
		StringBuffer oneDim = new StringBuffer();
		List<String> content = new ArrayList<String>();
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
						content.add(oneDim.toString());
						oneDim.delete(0, oneDim.length());
					}
					else if(c == ';'){
						content.add(oneDim.toString());
						oneDim.delete(0, oneDim.length());
						
						if(dimension != 0){
							if(dimension != content.size()){
								throw new InitiateException("Not matched dimension of static array in declaration. ");
							}
						}
						else{
							dimension = content.size();
						}
						
						if(head == null){
							String [] element = new String[content.size()];
							content.toArray(element);
							head = new BDPLArrayElement(element);
							current = head;
						}
						else{
							String [] element = new String[content.size()];
							content.toArray(element);
							temp = new BDPLArrayElement(element);
							current.setNext(temp);
							current = temp;
						}
						content.clear();
						
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
							if(dimension != content.size()){
								throw new InitiateException("Not matched dimension of static array in declaration. ");
							}
						}
						else{
							dimension = content.size();
						}
						
						if(head == null){
							String [] element = new String[content.size()];
							content.toArray(element);
							head = new BDPLArrayElement(element);
							current = head;
						}
						else{
							String [] element = new String[content.size()];
							content.toArray(element);
							temp = new BDPLArrayElement(element);
							current.setNext(temp);
							current = temp;
						}
						content.clear();
						
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
			content.add(oneDim.toString());
			oneDim.delete(0, oneDim.length());
		}
		
		if(content.size() > 0){
			if(dimension != 0){
				if(dimension != content.size()){
					throw new InitiateException("Not matched dimension of static array in declaration. ");
				}
			}
			else{
				dimension = content.size();
			}
			
			if(head == null){
				String [] element = new String[content.size()];
				content.toArray(element);
				head = new BDPLArrayElement(element);
				current = head;
			}
			else{
				String [] element = new String[content.size()];
				content.toArray(element);
				temp = new BDPLArrayElement(element);
				current.setNext(temp);
				current = temp;
			}
		}
		
		ret = new BDPLArray(head);
		return ret;
	}
	
	/*
	 * STATIC_QUERY
	 */
	private void make2(ArrayTableEntry entry, SubQueryTable table) throws InitiateException{
		
		List<String> varNames = null;
		
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
		}
		
		
		
		ISparqlRepository repo = new TestRepository();
		repo.start();
		
		BDPLArray array = new BDPLArray(repo.query(entry.getSource()));
		entry.setArray(array);

		
		/*//TODO repository management
		ISparqlRepository repo = new TestRepository();
		
		repo.start();
		TupleQueryResult result = (TupleQueryResult)repo.query(entry.getSource());
		BDPLArray array = new BDPLArray(null);
		entry.setArray(array);
		
		System.out.println(entry.getSource());
		
		try{
			
			while(result.hasNext()){
			
				BindingSet bindingSet = result.next();
				// TODO content type
				Set<String> names = bindingSet.getBindingNames();
				
				for(String name : names){
					System.out.println(name);
				}
				
				String [] content = new String[bindingSet.size()];
				
				for(int i = 0; i < varNames.size(); i++){
						System.out.println(varNames.get(i));
					content[i] = bindingSet.getValue(varNames.get(i)).stringValue();
				}
				
				array.write(content);
			}
		}
		catch(QueryEvaluationException qe){
			throw new InitiateException(qe.getMessage());
		}
		catch(BDPLArrayException be){
			throw new InitiateException(be.getMessage());
		}
		finally{
			try {
				result.close();
			} catch (QueryEvaluationException e) {

			}
		}*/
		
		repo.close();
	}
}
