package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_platform.platformservices.bdpl.VariableTypes;

/**
 * Generate code for variable representative. 
 * For each variable one query is needet which provides all possible values of this variable.
 * @author sobermeier
 *
 */
public class RdfQueryRepresentativeQueryVisitor extends GenericVisitor {

	private StringBuffer code;
	private String queryId;
	private Map<String, String> varRepresentative; // For each variable in the
													// query exists a
													// representative query.
													// This query is used to get
													// all possible values. E.g.
													// P : rdf(:e, P, O, 1234).

	public RdfQueryRepresentativeQueryVisitor(String queryId) {
		code = new StringBuffer();
		this.queryId = queryId;
	}
	
	// Start
	@Override
	public void visit(ElementEventGraph el) {
		code =new StringBuffer();
		varRepresentative = new HashMap<String, String>();
		
		// Visit triples
		el.getElement().visit(this);
	}

	@Override
	public void visit(ElementGroup el) {
		// Visit all group elements
		for (int i = 0; i < el.getElements().size(); i++) {
			el.getElements().get(i).visit(this);
		}
	}

	public Map<String, String> getRdfQueryRepresentativeQuery() {
		return varRepresentative;
	}

	@Override
	public Object visitBlank(Node_Blank it, AnonId id) {
		throw new RuntimeException("Node_Blank is not allowed.");
	}

	@Override
	public Object visitLiteral(Node_Literal it, LiteralLabel lit) {
		code.append("'" + lit.getLexicalForm() + "'");
		return lit;
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		code.append("'" + uri + "'");
		return uri;
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {

		StringBuffer resultNode = new StringBuffer();
		// It is part of a blank node.
		if (name.startsWith("?")) {
			// Transform number to uppercase char.
			for (int i = 1; i < name.length(); ++i) {
				char c = name.charAt(i);
				resultNode.append("V" + (char) (c + 17));
			}
			code.append(resultNode.toString());
			return resultNode.toString();
		} else {
			// It is a ordinary vraible.
			code.append("V" + name);
			return "V" + name;
		}
	}

	@Override
	public void visit(ElementPathBlock el) {
		Iterator<TriplePath> iter = el.getPattern().getList().iterator();

		// Generate representative queries for variables. Result will be stored
		// in "varRepresentative" variable.
		while (iter.hasNext()) {
			TriplePath tmpTriplePath = iter.next();
			generadeCodeForNewVariable(tmpTriplePath.getSubject(), tmpTriplePath);
			generadeCodeForNewVariable(tmpTriplePath.getPredicate(), tmpTriplePath);
			generadeCodeForNewVariable(tmpTriplePath.getObject(), tmpTriplePath);
		}

	}

	// Check if a representative exists for given Variable and generate code.;
	private void generadeCodeForNewVariable(Node node, TriplePath triple) {

		if (node.isVariable()) {
			String name = ((Node_Variable) node).getName();
			if (!varRepresentative.containsKey(name)) {
				code.append("rdf(");
				// Use existing visitors.
				triple.getSubject().visitWith(this); 
				code.append(",");
				triple.getPredicate().visitWith(this);
				code.append(",");
				triple.getObject().visitWith(this);
				code.append(")");
				
				varRepresentative.put(name, code.toString());
				code = new StringBuffer();
			}
		}
	}

}
