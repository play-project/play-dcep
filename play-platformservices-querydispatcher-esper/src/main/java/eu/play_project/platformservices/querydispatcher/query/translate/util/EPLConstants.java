/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate.util;

/**
 * @author ningyuan 
 * 
 * Apr 15, 2014
 *
 */
public class EPLConstants {
	
	public static final String SELECT = "select %s";
	
	public static final String FROM_PATTERN = "from %s pattern [ %s ]";
	
	public static final String EVERY = "every";
	
	public static final String TIMER_INTERVAL = "timer:interval";
	
	public static final String OPERATOR_SEQ = "->";
	
	public static final String OPERATOR_OR = "or";
	
	public static final String OPERATOR_AND = "and";
	
	public static final String OPERATOR_NOT = "not";
	
	public static final String TRIPLEEND = ".";
	
	public static final String EVENTTAG = "e";
	
	public static final String NOTEVENTTAG = "n";
	
	public static final String FILTER_RDF = "eu.play_project.platformservices.querydispatcher.query.filter.SesameRDFGraphFilter.evaluate(\"%s\"%s)";
	
	public static final String SPARQL_ASK_QUERY = "ASK WHERE { %s }";
	
	public static final String SPARQL_OPTIONAL_CLAUSE = "OPTIONAL { %s }";
	
	public static final String SPARQL_WHERE_CLAUSE = "WHERE { %s }";
	
	public static final String SPARQL_CLAUSE = "{ %s }";
	
	public static final String SPARQL_UNION = "UNION";
}
