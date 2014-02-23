package eu.play_project.querydispatcher.syntax_tree.tests;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventIterator;
import eu.play_project.querydispatcher.bdpl.tests.BdplEleTest;
import eu.play_project.querydispatcher.epsparql.tests.helpers.NestedEventsTreeVisitor;
import eu.play_project.querydispatcher.epsparql.tests.helpers.SimpleEvenTreeVisitor;

/**
 * Check if the produced tree has the expected structure.
 * @author Stefan Obermeier
 *	
 */
public class SyntaxTreeTests {
	
	@Test
	public void simpleTree() throws IOException {
		
		// Load query
		String queryString = getSparqlQuery("queries/SimpleTree.eprq");
		
		// Generate tree.
		Query query = null;
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}
		
		// Check result.
		String[] expectedResults = {"?e3", "SEQ", "?e4", "OR", "?e5"};
		SimpleEvenTreeVisitor v = new SimpleEvenTreeVisitor(expectedResults);
		query.getEventQuery().get(0).visit(v);
	}
	
	@Test
	public void nestedEvents() throws IOException {
		
		// Load query
		String queryString = getSparqlQuery("queries/NestedEvent.eprq");
		System.out.println(queryString);
		// Generate tree.
		Query query = null;
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception was thrown: " + e);
		}
		
		// Check result.
		String[] expectedResults = {"?e3", "SEQ", "(", "?e1", "OR", "?e2", ")"};
		NestedEventsTreeVisitor v = new NestedEventsTreeVisitor(expectedResults);
		query.getEventQuery().get(0).visit(v);
	}
	
	public static String getSparqlQuery(String queryFile) throws IOException {
		return IOUtils.toString(BdplEleTest.class.getClassLoader().getResourceAsStream(queryFile), StandardCharsets.UTF_8);
	}
}
