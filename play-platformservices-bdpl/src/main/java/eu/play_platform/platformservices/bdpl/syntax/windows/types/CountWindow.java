package eu.play_platform.platformservices.bdpl.syntax.windows.types;

import eu.play_platform.platformservices.bdpl.syntax.windows.Window;
import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;

public class CountWindow extends Window {
	
	@Override
	public void accept(ElementWindowVisitor v){
		v.visit(this);
	}
}
