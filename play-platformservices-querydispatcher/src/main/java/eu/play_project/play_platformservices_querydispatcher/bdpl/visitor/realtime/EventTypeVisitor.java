package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.Iterator;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;


public class EventTypeVisitor extends GenericVisitor implements ElementVisitor, NodeVisitor {
	private String eventType;

	@Override
	public void visit(ElementPathBlock el) {
		eventType = null;

		// Test if event type is defined.
		Iterator<TriplePath> iter = el.patternElts();
		while (iter.hasNext()) {
			TriplePath tmpTriplePath = iter.next();
			if(tmpTriplePath.getObject().isURI()){
				if (tmpTriplePath.getPredicate().equals(NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))){
					if(tmpTriplePath.getObject().isURI()){
						eventType =  "'" + tmpTriplePath.getObject().getURI() + "'";
					}else{
						throw new RuntimeException("Event type must be a URI");
					}
				}
			}
			}
		if(eventType == null){
			eventType = "'simple'"; // If no type is defined use simple.
		}
	}


	@Override
	public void visit(ElementGroup el) {
		// Visit all group elements
		for(int i=0; i<el.getElements().size(); i++){
			el.getElements().get(i).visit(this);
		}
	}

	@Override
	public void visit(ElementEventGraph el) {
		// Visit triples
		el.getElement().visit(this);
	}

	public String getEventType(){
		return eventType;
	}
}
