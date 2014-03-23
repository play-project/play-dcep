package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.Template;

public class ComplexTypeFinder extends GenericVisitor implements ElementVisitor, NodeVisitor {
	private String eventType;

	public String visit(Template constructTemplate){
		
		for (Triple triple : constructTemplate.getTriples()) {
			if (triple.getPredicate().equals(NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))){
				if(triple.getObject().isURI()){
					eventType = quoteForProlog(triple.getObject().getURI());
				}else{
					throw new RuntimeException("Event type must be a URI");
				}
			}
		}
		
		return eventType;
	}
}
