package eu.play_project.play_platformservices;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_PLACEHOLDER;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ontoware.rdf2go.model.QuadPattern;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.impl.StatementImpl;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.NamedVariable;
import eu.play_project.play_platformservices.api.QueryTemplate;

public class QueryTemplateImpl implements QueryTemplate<Statement, QuadPattern, URI>, Serializable {

	private final Logger logger = LoggerFactory.getLogger(QueryTemplateImpl.class);
	private static final long serialVersionUID = 100L;
	List<QuadPattern> templateQuads = new LinkedList<QuadPattern>();
	
	@Override
	public void appendLine(QuadPattern templateLine) {
		templateQuads.add(templateLine);
	}

	@Override
	public List<Statement> fillTemplate(HistoricalData historicalData, URI graph, URI eventId) {
		
		logger.debug("Filling template with values: {}", historicalData);
		
		List<Statement> result = new LinkedList<Statement>();

		for (QuadPattern templ : templateQuads) {
			Set<NodeOrVariable[]> t = new HashSet<NodeOrVariable[]>();
			NodeOrVariable[] templArray = new NodeOrVariable[] {templ.getContext(), templ.getSubject(), templ.getPredicate(), templ.getObject()};
			// PLAY requires the graph name of each event to be unique, i.e. it cannot be taken from the template:
			templArray[0] = graph;
			
			// The subject will also be replaced if it contains the placeholder ":e"
			if (templArray[1].toString().equals(EVENT_ID_PLACEHOLDER)) {
				templArray[1] = eventId;
			}
			
			t.add(templArray);
			result.addAll(fillTemplateHelper(t, historicalData, 0));
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
	 * @param historicalData
	 *            Variable names and their (multiple) values to be replaced in
	 *            the template.
	 * @param step
	 *            The recursion step... the method starts at step {@code 0} to
	 *            fill the RDF graph names moving on until step {@code 3}
	 *            filling the RDF objects.
	 * @return Returns a collection of quadruples fulfilling the variable
	 *         values.
	 */
	private List<Statement> fillTemplateHelper(Set<NodeOrVariable[]> t, HistoricalData historicalData, int step) {
		Set<NodeOrVariable[]> tNext = new HashSet<NodeOrVariable[]>();
		for (NodeOrVariable[] tempLine : t) {
			if (tempLine[step] instanceof NamedVariable) {
				for (String value : historicalData.get(((NamedVariable)tempLine[step]).getName())) {
					NodeOrVariable[] qNext = tempLine.clone();
					qNext[step] = EventHelpers.toRdf2GoNode(value);
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
			return fillTemplateHelper(tNext, historicalData, step + 1);
		}
		/*
		 *  ...or return results.
		 */
		else {
			List<Statement> result = new LinkedList<Statement>();
			
			for (NodeOrVariable[] quad : tNext) {
				result.add(new StatementImpl((URI)quad[0], (Resource)quad[1], (URI)quad[2], (Node)quad[3]));
			}
			
			return result;
		}
	}
}
