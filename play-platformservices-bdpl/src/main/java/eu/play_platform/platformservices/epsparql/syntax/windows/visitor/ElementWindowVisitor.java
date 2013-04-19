package eu.play_platform.platformservices.epsparql.syntax.windows.visitor;

import eu.play_platform.platformservices.epsparql.syntax.windows.types.CountWindow;
import eu.play_platform.platformservices.epsparql.syntax.windows.types.SlidingWindow;
import eu.play_platform.platformservices.epsparql.syntax.windows.types.TumblingWindow;

public interface ElementWindowVisitor {

	public void visit(CountWindow countWindow);
	public void visit(SlidingWindow slidingWindow);
	public void visit(TumblingWindow tumblingWindow);
}
