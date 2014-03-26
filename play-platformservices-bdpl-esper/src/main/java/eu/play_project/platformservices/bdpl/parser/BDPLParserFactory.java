/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;

/**
 * @author ningyuan
 *
 */
public class BDPLParserFactory implements QueryParserFactory  {
	
	// original querylanguage constants are defined in class QueryLanguage
	public static final QueryLanguage BDPL = new QueryLanguage("BDPL");
	
	private final BDPLParser singleton = new BDPLParser();

	/**
	 * Returns 
	 */
	public QueryLanguage getQueryLanguage() {
		return BDPL;
	}

	/**
	 * Returns a shared, thread-safe, instance of SPARQLParser.
	 */
	public QueryParser getParser() {
		return singleton;
	}
}
