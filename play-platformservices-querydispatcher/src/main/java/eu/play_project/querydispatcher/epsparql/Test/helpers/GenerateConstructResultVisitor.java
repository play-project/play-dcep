package eu.play_project.querydispatcher.epsparql.Test.helpers;

import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;

public class GenerateConstructResultVisitor implements NodeVisitor {
	@Override
	public Object visitAny(Node_ANY it) {
		return "'"+ it.toString() + "'";
	}

	@Override
	public Object visitBlank(Node_Blank it, AnonId id) {
		return "['http://blak.example.com/" + id + "']";
	}

	@Override
	public Object visitLiteral(Node_Literal it, LiteralLabel lit) {
		return "['" + lit + "']";
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		return "['" + uri + "']";
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		return "[V" + name + "]";
	}

}
