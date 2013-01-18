package eu.play_project.play_platformservices;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_PLACEHOLDER;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;

import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.QueryTemplate;
import fr.inria.eventcloud.api.Quadruple;

public class QueryTemplateImpl implements QueryTemplate {
	List<Quadruple> templateQuads = new LinkedList<Quadruple>();
	
	public void appendLine(Node graph, Node subject, Node predicate, Node object) {
		appendLine(new Quadruple(graph, subject, predicate, object));
	}

	public void appendLine(Quadruple line) {
		templateQuads.add(line);
	}

	public List<Quadruple> fillTemplate(Map<String, List<String>> variableBindings, Node graph, Node eventId) {
		
		List<Quadruple> result = new LinkedList<Quadruple>();

		for (Quadruple templ : templateQuads) {
			Set<Node[]> t = new HashSet<Node[]>();
			// PLAY requires the graph name of each event to be unique, i.e. it cannot be taken from the template:
			Node [] templArray = templ.toArray();
			templArray[0] = graph;
			
			// The subsject will also be replaced if it contains the placeholder ":e"
			if (templArray[1].toString().equals(EVENT_ID_PLACEHOLDER)) {
				templArray[1] = eventId;
			}
			
			t.add(templArray);
			result.addAll(fillTemplateHelper(t, variableBindings, 0));
		}
			
		return result;
	}
	
	/**
	 * Recursive method to go through each step (graph, subject, predicate,
	 * object) of the template quadruples and multiply each step (cross product)
	 * if there are more than one variable values.
	 * 
	 * @param t
	 *            Set of (partially filled template lines from the previous
	 *            step.
	 * @param variableBindings
	 *            Variable names and their (multiple) values to be replaced in
	 *            the template.
	 * @param step
	 *            The recursion step... the method starts at step {@code 0} to
	 *            fill the RDF graph names moving on until step {@code 3}
	 *            filling the RDF objects.
	 * @return Returns a collection of quadruples fulfilling the variable
	 *         values.
	 */
	private List<Quadruple> fillTemplateHelper(Set<Node[]> t, Map<String, List<String>> variableBindings, int step) {
		Set<Node[]> tNext = new HashSet<Node[]>();
		for (Node[] tempLine : t) {
			if (tempLine[step].isVariable()) {
				for (String value : variableBindings.get(tempLine[step].getName())) {
					Node[] qNext = tempLine.clone();
					qNext[step] = EventHelpers.toJenaNode(value);
					tNext.add(qNext);
				}
			}
			else {
				tNext.add(tempLine);
			}
		}

		/*
		 * Go into recursion...
		 */
		if (step < 3) {
			// Recursion to next step (e.g. moving on to filling triple subjects)
			return fillTemplateHelper(tNext, variableBindings, step + 1);
		}
		/*
		 *  ...or return results.
		 */
		else {
			List<Quadruple> result = new LinkedList<Quadruple>();
			
			for (Node[] quad : tNext) {
				result.add(new Quadruple(quad[0], quad[1], quad[2], quad[3]));
			}
			
			return result;
		}
	}
}
