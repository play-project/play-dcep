package eu.play_platform.platformservices.bdpl.syntax.windows.types;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

import eu.play_platform.platformservices.bdpl.syntax.windows.Window;
import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;

public class DummyWindow extends Window {

	@Override
	public void accept(ElementWindowVisitor v) {
		v.visit(this);
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
		return false;
	}

}
