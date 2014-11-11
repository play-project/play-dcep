package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import eu.play_platform.platformservices.bdpl.syntax.windows.types.CountWindow;
import eu.play_platform.platformservices.bdpl.syntax.windows.types.DummyWindow;
import eu.play_platform.platformservices.bdpl.syntax.windows.types.SlidingWindow;
import eu.play_platform.platformservices.bdpl.syntax.windows.types.TumblingWindow;
import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
import eu.play_project.play_platformservices.api.QueryDetailsEtalis;

public class WindowVisitor implements ElementWindowVisitor {
	QueryDetailsEtalis qd;
	
	public WindowVisitor(QueryDetailsEtalis qd){
		if(qd.getQueryId() == null || qd.getQueryId().equals("")){
			throw new IllegalArgumentException("Value for queryId is needet.");
		}
		this.qd = qd;
	}
	
	@Override
	public void visit(CountWindow countWindow) {
	}

	@Override
	public void visit(SlidingWindow slidingWindow) {
		qd.setEtalisProperty("([property(event_rule_window, "+ slidingWindow.getValue() + ")])");
		qd.setTumblingWindow("true");
	}

	@Override
	public void visit(TumblingWindow tumblingWindow) {
		qd.setTumblingWindow("tumbling_window('" + qd.getQueryId() + "', "+ tumblingWindow.getValue() + ")");
		qd.setEtalisProperty("");
	}

	@Override
	public void visit(DummyWindow dummyWindow) {
		qd.setTumblingWindow("true");
		qd.setEtalisProperty("");
	}
}