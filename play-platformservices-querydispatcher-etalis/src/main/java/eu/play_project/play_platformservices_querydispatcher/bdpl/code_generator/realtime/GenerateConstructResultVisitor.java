package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;

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
		return quoteForProlog(it.toString());
	}

	@Override
	public Object visitBlank(Node_Blank it, AnonId id) {
		return quoteForProlog("http://blak.example.com/" + id);
	}

	@Override
	public Object visitLiteral(Node_Literal it, LiteralLabel lit) {
		return quoteForProlog(lit.toString());
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		return quoteForProlog(uri);
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		return "V" + name + "";
	}

}
