package eu.play_platform.platformservices.bdpl.syntax.windows;

import org.slf4j.Logger;

import com.hp.hpl.jena.sparql.syntax.Element;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;

public abstract class Window extends Element{
	protected Logger logger;
	protected String value = "";
	
	public abstract void accept(ElementWindowVisitor v);
	
	public String getValue(){
		return value;
	}
}
