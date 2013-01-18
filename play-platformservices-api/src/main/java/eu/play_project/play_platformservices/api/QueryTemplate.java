package eu.play_project.play_platformservices.api;

import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;

/**
 * The QueryTemplate is used to represent quadruples with  a mixture of fixed values and variables.
 * 
 * @author sobermeier
 *
 */
public interface QueryTemplate {
	public void appendLine(Quadruple line);
	public void appendLine(Node graph, Node subject, Node predicate, Node object);
	public List<Quadruple> fillTemplate(Map<String, List<String>> variableBindings, Node graph, Node eventId);
}
