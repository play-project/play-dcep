package eu.play_project.querydispatcher.epsparql.tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.Join;
import eu.play_project.play_platformservices_querydispatcher.Variable;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.VariableVisitor;


public class JoinTest {
	
//	@Test
//	public void generateHistoricalQuery(){
//		String  queryString = "PREFIX : <http://example.com/> " +
//				"				CONSTRUCT{:e :type :FacebookCepResult." +
//				"							?s ?p ?o }" +
//				"				WHERE{ " +
//				"						EVENT ?id1{?s1 ?p ?o} " +
//				"						GRAPH ?id2{?s2 ?p ?o}" +
//				"				}";
//		//queryString = getQuery("EP-SPARQL-Query-Event-and-GRAPH.eprq");
//		
//		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
//		System.out.println(query);
//	}
	
	
	/**
	 * Print all variables and their types
	 */
	@Test
	public void getVariablesFromConstruct(){
		String  queryString = "" +
				"PREFIX : <http://example.com/> " +
				"PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"				CONSTRUCT{:e rdf:type :FacebookCepResult." +
				"							?s ?p ?o }" +
				"				WHERE{ " +
				"						EVENT ?id{?s ?p ?o." +
				"								  ?s rdf:type :test} " +
				"						GRAPH ?id{?s ?o ?o}}";

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		VariableVisitor visitor = new VariableVisitor();
		
		Map<String, List<Variable>>   resultVars = visitor.getVariables(query, eu.play_platform.platformservices.bdpl.VariableTypes.realtimeType);
		visitor.getVariables(query, eu.play_platform.platformservices.bdpl.VariableTypes.constructType, resultVars);
		visitor.getVariables(query, eu.play_platform.platformservices.bdpl.VariableTypes.historicType, resultVars);
		
		for(String key:resultVars.keySet()){
			System.out.print(key + " Type is: " );
			for (Variable var : resultVars.get(key)) {
				for (VariableTypes type :  var.getTypes()) {
					System.out.print(type + ", ");
				}
				System.out.println(" Int type: " + var.getType());
			}
			System.out.println();
		}
	}
	
	@Test
	public void getVariablesWithHistoricAndRealtimeType(){
		String  queryString = "PREFIX : <http://example.com/> " +
				"				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"				CONSTRUCT{:e rdf:type :FacebookCepResult." +
				"							?s ?p ?o}" +
				"				WHERE{ " +
				"						EVENT ?id{?s ?p ?f." +
				"								?s rdf:type :test} " +
				"						GRAPH ?id{?s ?o ?o FILTER (?f > \"1\")}}";

		// Parse query
		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		VariableVisitor visitor = new VariableVisitor();
		String[] expectedResults = {"f", "id", "s"}; // Variables which occours in historic and realtime part.
		
		Map<String, List<Variable>>   resultVars = visitor.getVariables(query, eu.play_platform.platformservices.bdpl.VariableTypes.realtimeType);
		visitor.getVariables(query, eu.play_platform.platformservices.bdpl.VariableTypes.constructType, resultVars);
		visitor.getVariables(query, eu.play_platform.platformservices.bdpl.VariableTypes.historicType, resultVars);
		int i=0;
		for(String key:resultVars.keySet()){
			for (Variable var : resultVars.get(key)) {
				if(var.getType()>=6){
					assertTrue(var.getName().equals(expectedResults[i]));
					i++;
				}
			}
		}
		
		
	}
	
	
	@Test
	public void join(){
		
		//Define tables to join
		Map<String, List<Variable>> r = new HashMap<String, List<Variable>>();
		Map<String, List<Variable>> s = new HashMap<String, List<Variable>>();
		
		//Define variables
		ArrayList<Variable> valueListR = new ArrayList<Variable>();
		valueListR.add(new Variable("a", eu.play_platform.platformservices.bdpl.VariableTypes.realtimeType, "vr1"));
		
		ArrayList<Variable> valueListS = new ArrayList<Variable>();
		Variable varS =  new Variable("a", eu.play_platform.platformservices.bdpl.VariableTypes.historicType, "vs1");
		varS.addValue("vs2");
		varS.addValue("vs3");
		varS.addValue("vs4");

		
		valueListS.add(varS);
		
		r.put("a", valueListR);
		s.put("a", valueListS);
		//join
		Join join = new Join();
		
		join.join(r, s);
		
		for (Variable variable : r.get("a")) {
			variable.getValues().get(0);

		}
	

		//assertTrue()
		
		
	}

	
	/**
	 * Return the query from given file. If given it returns the message of the expected exception.
	 * @param queryFile
	 * @return query[0] the query text, query[1] if given in input file the expected exception 
	 */
	public String getQuery(String queryFile) {
	
			InputStream is = null;
			try {
				is = (InputStream) getClass().getClassLoader().getResource(queryFile).getContent();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			StringBuffer sb = new StringBuffer();
			String line;
			String[] exeptionName = null;
			
			try {
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				br.close();
				isr.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			return sb.toString();
	}
}
