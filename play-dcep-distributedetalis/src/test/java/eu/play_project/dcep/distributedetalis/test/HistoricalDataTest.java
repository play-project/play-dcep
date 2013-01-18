package eu.play_project.dcep.distributedetalis.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerNet;
import eu.play_project.dcep.distributedetalis.api.HistoricalData;
import eu.play_project.dcep.distributedetalis.join.Engine;
import eu.play_project.dcep.distributedetalis.join.HistoricalQueryContainer;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import eu.play_project.play_platformservices.api.QueryDetails;

public class HistoricalDataTest {
	
	/**
	 * Check if a valid SPARQL-Query with given variables and values  is generated.
	 * @author sobermeier
	 */
	@Test
	public void valueBindingsTest(){
		//Basic query without value bindings.
		String query = "SELECT ?a ?b \n WHERE {GRAPH ?a{?b ?c ?d}}";
		
		//Some variables to bind.
		Map<String,List<String>> variablesValues = new HashMap<String, List<String>>();
		
		//Variable a
		List<String> aValues =  new LinkedList<String>();
		aValues.add("alice1");
		aValues.add("alice2");
		variablesValues.put("a", aValues);
		
		//Variable  b
		List<String> bValues =  new LinkedList<String>();
		bValues.add("bob1");
		bValues.add("bob2");
		variablesValues.put("b", bValues);
		
		//Variable  c
		List<String> cValues =  new LinkedList<String>();
		variablesValues.put("c", cValues);
		
		HistoricalQueryContainer hq = new HistoricalQueryContainer(query, variablesValues);

		System.out.println(hq.getQuery());
		
	}

	
	

}
