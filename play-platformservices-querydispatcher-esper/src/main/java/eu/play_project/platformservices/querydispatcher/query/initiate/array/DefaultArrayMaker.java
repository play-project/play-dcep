/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.initiate.array;


import java.util.List;

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
			
		}
		catch(MalformedQueryException me){
			
		}
		catch (ParseException e) {
			
		}
		
		
		//TODO repository management
		ISparqlRepository repo = new TestRepository();
		
		repo.start();
		TupleQueryResult result = (TupleQueryResult)repo.query(entry.getSource());
		BDPLArray array = entry.getArray();
		
		try{
			
			while(result.hasNext()){
			
				BindingSet bindingSet = result.next();
				// TODO content type
				
				String [] content = new String[bindingSet.size()];
				
				for(int i = 0; i < varNames.size(); i++){
					content[i] = bindingSet.getValue(varNames.get(i)).stringValue();
				}
				
				array.write(content);
			}
		}
		catch(QueryEvaluationException qe){
			
		}
		catch(BDPLArrayException be){
			
		}
		
		repo.close();
	}
}
