package eu.play_platform.platformservices.epsparql.tests;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;


public class EpsparqlTest {
	public final String NL = System.getProperty("line.separator");

	public enum TestType {
		EP_SPARQL_Query, EP_SPARQL_BROKEN_QUERY
	}
	
	@Ignore
	@Test
	public void manualTest(){
		QueryFactory.create("PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> CONSTRUCT{ rdf:abc rdf:type rdf:name } WHERE{ EVENT ?id{?A  ?B ?C} FILTER contains(?A , 'dddd')}", Syntax.syntaxBDPL);
	}
		
	@Test
	public void testPositiveTests() {
				
		for (String fileName : new String[] {
				"EP-SPARQL-Query-HAVING.eprq",
				"EP-SPARQL-Query-CONSTRUCT-Query.eprq",
				"EP-SPARQL-Query-Event-and-GRAPH.eprq",
				"EP-SPARQL-Query-FILTER-contains-possition.eprq",
				"EP-SPARQL-Query-FILTER-contains.eprq",
				"EP-SPARQL-Query-Historic-followed-by-Realtime.eprq",
				"EP-SPARQL-Query-M12-Construct-Query.eprq",
				"EP-SPARQL-Query-more-than-one-FILTER.eprq",
				"EP-SPARQL-Query-New-Infix-Opators.eprq",
				"EP-SPARQL-Query-Realtime-Historical-multiple-Clouds.eprq",
				"EP-SPARQL-Query-Realtime-Historical-shared-Variables.eprq",
				// "EP-SPARQL-Query-SEQ-Parenthesis.eprq", Currently not needed.
				"EP-SPARQL-Query-Type.eprq",
				"EP-SPARQL-Query-WINDOW-sliding.eprq",
				"EP-SPARQL-Query-WINDOW-tumbling.eprq",
				"play-bdpl-crisis-01a-radiation.eprq",
				"play-bdpl-crisis-01b-radiationincrease.eprq",
				"play-bdpl-crisis-02a-winddirection.eprq",
				"play-bdpl-crisis-02b-windintensity.eprq",
				"play-bdpl-crisis-03-drawgraph.eprq",
				"play-epsparql-clic2call.eprq",
				"play-epsparql-clic2call-plus-tweet.eprq",
				"play-epsparql-contextualized-latitude-01-query.eprq",
				"play-epsparql-m12-jeans-example-query.eprq",
				"play-epsparql-telco-recom.eprq",
				"play-epsparql-telco-recom-tweets.eprq",
				"play-epsparql-iccs-telco-02.eprq",
				"play-epsparql-iccs-telco-02a.eprq",
				"play-bdpl-personalmonitoring-01-slowdown-recom.eprq",
				"play-bdpl-personalmonitoring-02-slowdown-recom-two-events.eprq",
				"play-bdpl-personalmonitoring-03-related-location.eprq",
				"play-bdpl-personalmonitoring-04-slowdown-recom-three-events.eprq",
				}) {
			System.out.println("Testing queryfile: " + fileName);
			try {
				QueryFactory.create(getQuery(fileName)[0], Syntax.syntaxBDPL);
			} catch (IOException e) {
				fail("Could not read query file: " + fileName);
			} catch (QueryException e) {
				e.printStackTrace();
				fail("Malformed query file: " + fileName + " with error: " + e.getMessage());
			}
		}
	}
	

	@Test
	public void testNegativeTests() throws Exception {

		String[] query;
		
		for (String fileName : getFilenames(new File("src/test/resources/"), TestType.EP_SPARQL_BROKEN_QUERY)) {
			System.out.println("Testing queryfile: " + fileName);
			try {
				query = getQuery(fileName);
				// Test if expeted exeption is given.
				if(query[1]==null){
					throw new Exception("No expected exception given in inputfile " + fileName + " \t e.g. @expectedException<java.lang.NullPointerException\\>");
				}
				
				try {
					//QueryFactory.create(query[0], Syntax.syntaxBDPL); // TODO test whats wrong
	
				} catch (QueryParseException e) {
					if (!e.getMessage().contains(query[1])) { // Test if expected exception
						e.printStackTrace();
						throw new Exception("Not expected exception was thrown in " + fileName + "\n" + query[1] + " is expected");
					}
				}
			} catch (IOException e1) {
				fail("Could not read query file: " + fileName);
			}

		}

		// Assert.assertTrue(numberOfFailedTests==negativeTestFiles.length);
	}
	
	/**
	 * Return the query from given file. If given it returns the message of the expected exception.
	 * @param queryFile
	 * @return query[0] the query text, query[1] if given in input file the expected exception
	 * @throws IOException
	 */
	public String[] getQuery(String queryFile) throws IOException {
		System.out.println(queryFile );
		InputStream is = (InputStream) getClass().getClassLoader().getResource(queryFile).getContent();
		
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
				sb.append("\n");
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
	}
	
	/**
	 * Returns the filenaes of the testfiles depending on the type of the testfile.
	 * The filename of a file with contains a broken query (a query which the parser do not acceapt) must start with "EP-SPARQL-BrokenQuery".
	 * The filename of a file with a regular query must start with "EP-SPARQL-Query".
	 * @param dir Directory of the files.
	 * @param type Type of the fiel.
	 * @return All filenames with the specified type.
	 */
	public static List<String> getFilenames(File dir, TestType type) {

		ArrayList<String> filenames = new ArrayList<String>();

		File[] files = dir.listFiles(); // FIXME sobermeier test this from inside jar
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if(type == TestType.EP_SPARQL_BROKEN_QUERY){
					if (files[i].getName().startsWith("EP-SPARQL-BrokenQuery")
							&& !files[i].isDirectory()) {
						filenames.add(files[i].getName());
					}
				}else if (type == TestType.EP_SPARQL_Query){
					if (files[i].getName().startsWith("EP-SPARQL-Query")
							&& !files[i].isDirectory()) {
						filenames.add(files[i].getName());
				
					}
				}
			}
		}
		return filenames;
	}
}
