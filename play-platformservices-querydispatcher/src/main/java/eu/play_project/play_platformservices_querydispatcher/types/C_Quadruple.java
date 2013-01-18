package eu.play_project.play_platformservices_querydispatcher.types;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.api.Quadruple;

public class C_Quadruple extends Quadruple{

	public C_Quadruple(Node graph, Node subject, Node predicate, Node object) {
		super(graph, subject, predicate, object);
	}
	
	public C_Quadruple(Node createURI, Triple triple) {
		super(createURI, triple);
	}

}
