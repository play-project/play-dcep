package eu.play_project.querydispatcher.bdpl.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;
import fr.inria.eventcloud.api.Quadruple;

public class TypeSystemTest {
	
	/**
	 * Set different types and retrieve them.
	 */
	@Test
	public void testFindType(){
		VariableTypeManager vm = new VariableTypeManager(null);
		
		// Set types.
		vm.addVariable("a", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("a", VariableTypes.AVG_TYPE);
		
		// Check if it is possible to retrieve informations.
		assertTrue(vm.isType("a", VariableTypes.CONSTRUCT_TYPE));
		assertTrue(vm.isType("a", VariableTypes.AVG_TYPE));
		
		assertFalse(vm.isType("a", VariableTypes.HISTORIC_TYPE));
		assertFalse(vm.isType("a", VariableTypes.REALTIME_TYPE));
		assertFalse(vm.isType("a", VariableTypes.MIN_TYPE));
	}
	
	@Test
	public void testGetAllVariablesOfOneType(){
		VariableTypeManager vm = new VariableTypeManager(null);
		
		// Set types.
		vm.addVariable("a", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("a", VariableTypes.REALTIME_TYPE);
		vm.addVariable("a", VariableTypes.AVG_TYPE);
		
		vm.addVariable("b", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("b", VariableTypes.REALTIME_TYPE);
		vm.addVariable("b", VariableTypes.MIN_TYPE);
		
		vm.addVariable("c", VariableTypes.CONSTRUCT_TYPE);
		vm.addVariable("c", VariableTypes.REALTIME_TYPE);
		vm.addVariable("c", VariableTypes.MAX_TYPE);
		
		List<String> vars = vm.getVariables(VariableTypes.REALTIME_TYPE);
		assertTrue(vars.size()==3);
		assertTrue(vars.contains("a"));
		assertTrue(vars.contains("b"));
		assertTrue(vars.contains("c"));
		
		vars = vm.getVariables(VariableTypes.MAX_TYPE);
		assertTrue(vars.size()==1);
		assertTrue(vars.contains("c"));
	}
	
	@Test
	public void testQueryTemplateGeneration() throws IOException {

		String queryString = BdplEleTest.getSparqlQuery("queries/HistoricRealtimeQuery.eprq");
		Query query = null;

		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {
			System.out.println(e);
		}

		QueryTemplateGenerator queryTemplateGenerator = new QueryTemplateGenerator();
		
		QueryTemplate queryTemplate = queryTemplateGenerator.createQueryTemplate(query);
		
		HistoricalData historicalData = new HistoricalData();
		List<String> values = new LinkedList<String>();
		values.add("value1");
		values.add("value2");
		values.add("value3");
		
		historicalData.put("screenName01", values);
		List<Quadruple> result = queryTemplate.fillTemplate(historicalData, NodeFactory.createURI("http://example.com/tests") ,  NodeFactory.createURI("http://example.com/eventId"));
		
		assertTrue((result.get(0).getObject().equals(NodeFactory.createLiteral("value3")) || result.get(0).getObject().equals(NodeFactory.createLiteral("value2")) || result.get(0).getObject().equals(NodeFactory.createLiteral("value1"))));
		assertTrue((result.get(1).getObject().equals(NodeFactory.createLiteral("value3")) || result.get(1).getObject().equals(NodeFactory.createLiteral("value2")) || result.get(1).getObject().equals(NodeFactory.createLiteral("value1"))));
		assertTrue((result.get(2).getObject().equals(NodeFactory.createLiteral("value3")) || result.get(2).getObject().equals(NodeFactory.createLiteral("value2")) || result.get(2).getObject().equals(NodeFactory.createLiteral("value1"))));
	}
	
	/**
	 * Return the query from given file. If given it returns the message of the
	 * expected exception.
	 * 
	 * @param queryFile
	 * @return query[0] the query text, query[1] if given in input file the
	 *         expected exception
	 */
	public String[] getQuery(String queryFile) {
		try {
			InputStream is = (InputStream) getClass().getClassLoader().getResource(queryFile)
					.getContent();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			StringBuffer sb = new StringBuffer();
			String line;
			String[] exeptionName = null;

			while ((line = br.readLine()) != null) {

				if (line.startsWith("#")) { // Line with comment
					sb.append(line);
					sb.append("\n"); // new Line after commentline
					// Extract exceptionname
					exeptionName = line.split(".*@expectedException<*| *\\\\>*");
				} else {
					sb.append(line);
				}
			}
			// System.out.println(sb.toString());
			br.close();
			isr.close();
			is.close();

			if (exeptionName != null && exeptionName.length > 1) {
				return new String[] { sb.toString(), exeptionName[1] };
			} else {
				return new String[] { sb.toString(), null };
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the filenaes of the testfiles depending on the type of the
	 * testfile. The filename of a file with contains a broken query (a query
	 * which the parser do not acceapt) must start with "BDPL-BrokenQuery". The
	 * filename of a file with a regular query must start with "BDPL-Query".
	 * 
	 * @param dir
	 *            Directory of the files.
	 * @param type
	 *            Type of the fiel.
	 * @return All filenames with the specified type.
	 */
	public static List<String> getFilenames(File dir) {

		ArrayList<String> filenames = new ArrayList<String>();

		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().endsWith(".ele") && !files[i].isDirectory()) {
					filenames.add(files[i].getName());
				}
			}
		}
		return filenames;
	}

}
