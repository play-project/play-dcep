package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import com.hp.hpl.jena.query.Query;

/**
 * Generate code for CEP-Engine
 * @author fzi
 *
 */
public interface CepCodeGenerator {
	
	public String generateCode(Query query, String patternId);

}
