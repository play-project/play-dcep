package eu.play_project.play_platformservices_querydispatcher.types;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.api.Quadruple;

public class R_Quadruple extends Quadruple {

	public R_Quadruple(Node graph, Node subject, Node predicate, Node object) {
		super(graph, subject, predicate, object);
	}
	
	public R_Quadruple(Node createURI, Triple triple) {
		super(createURI, triple);
	}

}
