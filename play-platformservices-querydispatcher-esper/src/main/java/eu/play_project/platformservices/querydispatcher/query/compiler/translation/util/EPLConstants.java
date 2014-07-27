/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.util;

/**
 * @author ningyuan 
 * 
 * Apr 15, 2014
 *
 */
public class EPLConstants {
	
	public static final String SELECT = "select %s";
	
	public static final String FROM_PATTERN = "from %s pattern [ %s ]";
	
	public static final String WINDOW_SLIDING = ".win:time(%s sec)";
	
	public static final String WINDOW_TUMBLING = ".win:time_batch(%s sec)";
	
	public static final String EVERY = "every";
	
	public static final String TIMER_INTERVAL_NAME = "timer:interval";
			
	public static final String TIMER_INTERVAL = TIMER_INTERVAL_NAME+"(%s sec)";
	
	public static final String OPERATOR_SEQ = "->";
	
	public static final String OPERATOR_OR = "or";
	
	public static final String OPERATOR_AND = "and";
	
	public static final String OPERATOR_NOT = "not";
	
	public static final String TRIPLEEND = ".";
	
	public static final String EVENTTAG = "e";
	
	public static final String NOTEVENTTAG = "n";
	
	public static final String FILTER_RDF = "eu.play_project.platformservices.querydispatcher.query.compiler.translation.filter.RDFGraphEventFilter.evaluate(\"%s\"%s)";
	
	public static final String FILTER_VAR_BINDING = "eu.play_project.platformservices.querydispatcher.query.compiler.translation.filter.RealTimeResultBindingFilter.evaluate(\"%s\"%s)";
	
	public static final String SPARQL_UNION = "UNION";
}
