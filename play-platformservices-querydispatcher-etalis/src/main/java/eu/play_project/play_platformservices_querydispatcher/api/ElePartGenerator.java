package eu.play_project.play_platformservices_querydispatcher.api;

import com.hp.hpl.jena.query.Query;

/**
 * Generate parts of an ELE query.
 * @author sobermeier
 *
 */
public interface ElePartGenerator {
	public void generateCode(Query q);
}
