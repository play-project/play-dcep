package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

public class CollectVariablesInTriplesVisitor extends GenericVisitor{
	
	private Set<String> vars; 

	
	@Override
	public void visit(ElementEventGraph el) {
		vars = new HashSet<String>();	
		// Visit triples
		el.getElement().visit(this);
	}

	@Override
	public void visit(ElementGroup elg) {
		// Visit all group elements
		for (Element el : elg.getElements()) {
			el.visit(this);
		}
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {

		if(!name.startsWith("?")){ //Ignore blank nodes
			vars.add("V" + name);
		}

		return it;
	}

	@Override
	public void visit(ElementPathBlock el) {
		for (TriplePath tp : el.getPattern().getList()) {
			tp.getSubject().visitWith(this);
			tp.getPredicate().visitWith(this);
			tp.getObject().visitWith(this);
		}
	}

	/**
	 * Return all variables used in this query.
	 */
	public  Set<String> getVariables(){
		return vars;
	}


}
