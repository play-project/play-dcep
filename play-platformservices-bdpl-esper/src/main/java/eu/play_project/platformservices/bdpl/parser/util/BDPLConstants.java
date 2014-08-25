/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;

/**
 * Constants in BDPL
 * 
 * @author ningyuan 
 * 
 * Jun 30, 2014
 *
 */
public class BDPLConstants {
	
	public static final String URI_STREAM = "http://events.event-processing.org/types/stream";
	
	public static final String URI_ENDTIME = "http://events.event-processing.org/types/endTime";
	
	public static final String SPARQL_ASK_QUERY = "ASK WHERE { %s }";
	
	public static final String SPARQL_OPTIONAL_CLAUSE = "OPTIONAL { %s }";
	
	public static final String SPARQL_WHERE_CLAUSE = "WHERE { %s }";
	
	public static final String SPARQL_CLAUSE = "{ %s }";
	
	public static final String SPARQL_FILTER_VAR_NOT_EQUAL = "FILTER (!sameTerm(?%s, ?%s))";
	
	public static final String SPARQL_FILTER_ENDTIME_EARLER = "FILTER (?%s < ?%s)";
}
