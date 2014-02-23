package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;

public class EventIterator extends GenericVisitor implements Iterable<Element>, Iterator<Element> {

	@Override
	public Iterator<Element> iterator() {
		return null;
	}


	// Methods from interface Iterator

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public Element next() {
		return null;
	}

	@Override
	public void remove() {
		// No remove.
	}
	

	@Override
	public void visit(ElementEventBinOperator el) {
		System.out.println(el.getLeft().getClass().getName());
		el.getLeft().visit(this);
		
		System.out.println("'" + el.getTyp() + "'");
		el.getRight().visit(this);
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		System.out.println(el.getElement());
	}

}
