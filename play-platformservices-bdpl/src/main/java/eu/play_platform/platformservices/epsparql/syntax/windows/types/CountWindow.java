package eu.play_platform.platformservices.epsparql.syntax.windows.types;

import eu.play_platform.platformservices.epsparql.syntax.windows.Window;
import eu.play_platform.platformservices.epsparql.syntax.windows.visitor.ElementWindowVisitor;

public class CountWindow extends Window {
	
	@Override
	public void accept(ElementWindowVisitor v){
		v.visit(this);
	}
}
