package eu.play_platform.platformservices.bdpl.syntax.windows;

import org.slf4j.Logger;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;

public abstract class Window {
	protected Logger logger;
	protected String value = "";
	
	public abstract void accept(ElementWindowVisitor v);
	
	public String getValue(){
		return value;
	}
}
