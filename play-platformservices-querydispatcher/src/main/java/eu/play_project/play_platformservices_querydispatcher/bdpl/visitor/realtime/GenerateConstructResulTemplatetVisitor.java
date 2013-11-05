package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;

import fr.inria.eventcloud.api.Quadruple;

public class GenerateConstructResulTemplatetVisitor extends GenericVisitor {
	Quadruple template = null;

	
	@Override
	public Object visitAny(Node_ANY it) {
		return it;
	}

	@Override
	public Object visitBlank(Node_Blank it, AnonId id) {
		template = new Quadruple(it, it, it ,it);
		return it;
	}

	@Override
	public Object visitLiteral(Node_Literal it, LiteralLabel lit) {
		template = new Quadruple(it, it, it ,it);
		return it;
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		template = new Quadruple(it, it, it ,it);
		return it;
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		template = new Quadruple(it, it, it ,it);
		return it;
	}

	public Quadruple getTemplate() {
		return template;
	}

}
