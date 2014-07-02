/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.sparql;



/**
 * @author ningyuan 
 * 
 * Jul 2, 2014
 *
 */
public interface SparqlRepository {
	
	public void start();
	
	public Object query(String query);
	
	public void close();
}
