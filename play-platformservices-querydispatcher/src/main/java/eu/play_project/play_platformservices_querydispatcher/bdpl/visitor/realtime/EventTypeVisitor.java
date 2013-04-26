package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.BooleanOperator;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventFilter;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFetch;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.RelationalOperator;


public class EventTypeVisitor extends GenericVisitor implements ElementVisitor, NodeVisitor {
	private String eventType;

	@Override
	public Object visitAny(Node_ANY it) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBlank(Node_Blank it, AnonId id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitLiteral(Node_Literal it, LiteralLabel lit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void visit(ElementNamedGraph el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementPathBlock el) {
		eventType = null;

		// Test if event type is defined.
		Iterator<TriplePath> iter = el.patternElts();
		while (iter.hasNext()) {
			TriplePath tmpTriplePath = iter.next();
			if(tmpTriplePath.getObject().isURI()){
				if (tmpTriplePath.getPredicate().equals(Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))){
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
	public void visit(ElementFilter el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementAssign el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementBind el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementUnion el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementOptional el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementGroup el) {
		// Visit all group elements
		for(int i=0; i<el.getElements().size(); i++){
			el.getElements().get(i).visit(this); 
		}		
	}

	@Override
	public void visit(ElementDataset el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementExists el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementNotExists el) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void visit(ElementService el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementFetch el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementSubQuery el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementEventGraph el) {
		// Visit triples
		el.getElement().visit(this);
	}

	@Override
	public void visit(ElementEventBinOperator el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementEventFilter el) {
		// TODO Auto-generated method stub
		
	}
	
	public String getEventType(){
		return eventType;
	}



	@Override
	public void visit(RelationalOperator arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BooleanOperator arg0) {
		// TODO Auto-generated method stub
		
	}

	
}
