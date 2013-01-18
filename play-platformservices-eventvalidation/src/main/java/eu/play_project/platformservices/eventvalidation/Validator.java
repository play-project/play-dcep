package eu.play_project.platformservices.eventvalidation;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;

import java.util.HashMap;
import java.util.Map;

import org.event_processing.events.types.Event;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * This class holds some reusable methods to check events before they enter the
 * PLAY system. We check for some sanity in term of minimum requirements for
 * what an event needs.
 * 
 * @author stuehmer
 * @author lpellegrino
 * 
 */
public class Validator {
	
	private Map<String, Boolean> propertyTestResults = new HashMap<String, Boolean>();
	private Map<String, Boolean> otherTestResults = new HashMap<String, Boolean>();

	public Validator() {
		this.propertyTestResults.put(Event.ENDTIME.toString(), false);
		this.propertyTestResults.put(Event.STREAM.toString(), false);
		this.propertyTestResults.put(Event.TYPE.toString(), false);

		this.otherTestResults.put("EventIdTest", false);
		// Avoiding negation as failure:
		this.otherTestResults.put("BlankNodeTest", true);
	}

	/**
	 * Add a quadruple to the test before invoking {@link #isValid()}. This
	 * method uses RDF2Go objects as parameter.
	 */
	public Validator checkQuadruple(URI context, Resource subject, URI predicate, org.ontoware.rdf2go.model.node.Node object) {

		if (subject instanceof BlankNode || object instanceof BlankNode) {
			this.otherTestResults.put("BlankNodeTest", false);
		}

		this.checkQuadruple(
				context.toString(), subject.toString(), 
				predicate.toString(), object.toString());
		return this;
	}

	/**
	 * Add a quadruple to the test before invoking {@link #isValid()}. This
	 * method (unlike {@link #checkQuadruple(Statement)}) allows using
	 * {@linkplain Statement}s which are not quadruples by augmenting an extra
	 * {@linkplain URI} to make up for the missing context (graph name) part.
	 */
	public Validator checkQuadruple(URI context, Statement s) {
		this.checkQuadruple(
				context, s.getSubject(), 
				s.getPredicate(), s.getObject());
		return this;
	}
	
	/**
	 * Add a quadruple to the test before invoking {@link #isValid()}. This
	 * method assumes you are using {@linkplain Statement}s which are quadruples(!)
	 * e.g., from a {@linkplain org.ontoware.rdf2go.model.ModelSet}.
	 */
	public Validator checkQuadruple(Statement s) {
		this.checkQuadruple(
				s.getContext(), s.getSubject(), 
				s.getPredicate(), s.getObject());
		return this;
	}
	
	/**
	 * Add a quadruple to the test before invoking {@link #isValid()}.
	 */
	public Validator checkQuadruple(Quad q) {
		this.checkQuadruple(
				q.getGraph(), q.getSubject(), 
				q.getPredicate(), q.getObject());
		return this;
	}

	/**
	 * Add a quadruple to the test before invoking {@link #isValid()}.
	 */
	public Validator checkQuadruple(Node g, Triple t) {
		this.checkQuadruple(
				g, t.getSubject(), 
				t.getPredicate(), t.getObject());
		return this;
	}

	/**
	 * Add a quadruple to the test before invoking {@link #isValid()}.
	 */
	public Validator checkQuadruple(Node g, Node s, Node p, Node o) {

		if (s.isBlank() || o.isBlank()) {
			this.otherTestResults.put("BlankNodeTest", false);
		}		
		
		this.checkQuadruple(
				g.toString(), s.toString(), 
				p.toString(), o.toString());
		return this;
	}

	/**
	 * Add a quadruple to the test before invoking {@link #isValid()}.
	 */
	private Validator checkQuadruple(String g, String s, String p, String o) {
		if (this.propertyTestResults.containsKey(p)) {
			this.propertyTestResults.put(p, true);
		}

		if (p.equals(Event.TYPE.toString())) {
			this.otherTestResults.put("EventIdTest", s.equals(g + EVENT_ID_SUFFIX));
		}
		return this;
	}

	/**
	 * Add a model (set of quadruples) to the test before invoking
	 * {@link #isValid()}. This method (unlike {@link #checkModel(Model)})
	 * allows using {@linkplain Model}s which do not contain quadruples by
	 * augmenting an extra {@linkplain URI} to make up for the missing context
	 * (graph name) part.
	 */
	public Validator checkModel(URI context, Model m) {
		for (Statement s : m) {
			checkQuadruple(context, s);
		}	
	return this;	
	}
	
	/**
	 * Add a model (set of quadruples) to the test before invoking
	 * {@link #isValid()}. This method assumes you are using {@linkplain Model}s
	 * which contain quadruples(!) e.g., from a
	 * {@linkplain org.ontoware.rdf2go.model.ModelSet}.
	 */
	public Validator checkModel(Model m) {
		for (Statement s : m) {
			checkQuadruple(s);
		}
		return this;
	}
	
	/**
	 * Compute all property tests conjunctively.
	 * 
	 * @return {@code true} if it is valid, throws exception otherwise.
	 * @throws InvalidEventException 
	 */
	public boolean isValid() throws InvalidEventException {
		boolean result = true;
		
		String propertiesResults = "Missing properties: ";
		for (String propertiesTest : this.propertyTestResults.keySet()) {
			if (this.propertyTestResults.get(propertiesTest) == false) {
				result = false;
				propertiesResults += propertiesTest + " ";
			}
		}
		propertiesResults += "... ";

		String otherResults = "Other failures: ";
		for (String otherTest : this.otherTestResults.keySet()) {
			if (this.otherTestResults.get(otherTest) == false) {
				result = false;
				otherResults += otherTest + " ";
			}
		}
		otherResults += "... ";
		
		if (result != true ) {
			throw new InvalidEventException("Invalid event... " + propertiesResults + otherResults);
		}
		
		return result;
	}
	
}
