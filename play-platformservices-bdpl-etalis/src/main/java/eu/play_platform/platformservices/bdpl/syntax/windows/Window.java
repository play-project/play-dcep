package eu.play_platform.platformservices.bdpl.syntax.windows;

import org.slf4j.Logger;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;

public abstract class Window extends Element{
	protected Logger logger;
	protected String value = "";
	
	public abstract void accept(ElementWindowVisitor v);
	
	public String getValue(){
		return value;
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
