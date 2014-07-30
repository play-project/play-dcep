package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunction3;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitor;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDT;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDuration;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueFloat;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementBraceOperator;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementDuration;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.RelationalOperator;

import eu.play_platform.platformservices.bdpl.syntax.windows.types.CountWindow;
import eu.play_platform.platformservices.bdpl.syntax.windows.types.DummyWindow;
import eu.play_platform.platformservices.bdpl.syntax.windows.types.SlidingWindow;
import eu.play_platform.platformservices.bdpl.syntax.windows.types.TumblingWindow;



public class GenericVisitor implements ElementVisitor, NodeVisitor, ExprVisitor, NodeValueVisitor{
	Logger logger;

	public GenericVisitor(){
		logger = LoggerFactory.getLogger(GenericVisitor.class);
	}
	@Override
	public void visit(ElementNamedGraph el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementPathBlock el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementFilter el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementAssign el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementBind el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementUnion el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementOptional el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementGroup el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementDataset el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementExists el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementNotExists el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementService el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementSubQuery el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementEventGraph el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementEventBinOperator el) {
		logger.debug("Visit " + el.getClass().getName());
		
	}
	
	@Override
	public void visit(RelationalOperator arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
		
	}

	@Override
	public void visit(com.hp.hpl.jena.sparql.syntax.ElementFnAbsFilter arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
		
	}

	@Override
	public Object visitAny(Node_ANY arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
		return null;
	}
	
	@Override
	public Object visitBlank(Node_Blank arg0, AnonId id) {
		logger.debug("Visit " + arg0.getClass().getName());
		return null;
	}
	
	@Override
	public Object visitLiteral(Node_Literal arg0, LiteralLabel lit) {
		logger.debug("Visit " + arg0.getClass().getName());
		return null;
	}
	
	@Override
	public Object visitURI(Node_URI it, String uri) {
		// logger.debug("Visit " + it.getClass().getName());
		return null;
	}
	
	@Override
	public Object visitVariable(Node_Variable it, String name) {
		logger.debug("Visit " + it.getClass().getName());
		return null;
	}
	
	@Override
	public void startVisit() {
		logger.debug("startVisit");
	}
	
	@Override
	public void visit(ExprFunction0 arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
		
	}
	
	@Override
	public void visit(ExprFunction1 arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void visit(ExprFunction2 arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void visit(ExprFunction3 arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void visit(ExprFunctionN arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void visit(ExprFunctionOp arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void visit(NodeValue arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void visit(ExprVar arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void visit(ExprAggregator arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
	}
	
	@Override
	public void finishVisit() {
		logger.debug("finishVisit()");
	}
	
	@Override
	public void visit(ElementData arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
		
	}
	
	@Override
	public void visit(ElementMinus arg0) {
		logger.debug("Visit " + arg0.getClass().getName());
		
	}
	
	@Override
	public void visit(NodeValueBoolean nv) {
		logger.debug("Visit " + nv.getClass().getName());		
	}
	
	@Override
	public void visit(NodeValueDecimal nv) {
		logger.debug("Visit " + nv.getClass().getName());
	}
	
	@Override
	public void visit(NodeValueDouble nv) {
		logger.debug("Visit " + nv.getClass().getName());
	}
	
	@Override
	public void visit(NodeValueFloat nv) {
		logger.debug("Visit " + nv.getClass().getName());
	}
	
	@Override
	public void visit(NodeValueInteger nv) {
		logger.debug("Visit " + nv.getClass().getName());
	}
	
	@Override
	public void visit(NodeValueNode nv) {
		logger.debug("Visit " + nv.getClass().getName());
	}
	
	@Override
	public void visit(NodeValueString nv) {
		logger.debug("Visit " + nv.getClass().getName());
	}
	
	@Override
	public void visit(NodeValueDT nv) {
		logger.debug("Visit " + nv.getClass().getName());
	}
	
	@Override
	public void visit(NodeValueDuration nodeValueDuration) {
		logger.debug("Visit " + nodeValueDuration.getClass().getName());
	}
	
	@Override
	public void visit(ElementNotOperator elementNotOperator) {
		logger.debug("Visit " + elementNotOperator.getClass().getName());	
	}
	
	@Override
	public void visit(CountWindow countWindow) {
		logger.debug("Visit " + countWindow.getClass().getName());	
	}
	
	@Override
	public void visit(SlidingWindow slidingWindow) {
		logger.debug("Visit " + slidingWindow.getClass().getName());	
	}
	
	@Override
	public void visit(TumblingWindow tumblingWindow) {
		logger.debug("Visit " + tumblingWindow.getClass().getName());	
		
	}
	
	@Override
	public void visit(DummyWindow dummyWindow) {
		logger.debug("Visit " + dummyWindow.getClass().getName());	
	}
	
	@Override
	public void visit(ElementBraceOperator elementBraceOperator) {
		logger.debug("Visit " + elementBraceOperator.getClass().getName());	
	}
	@Override
	public void visit(ElementDuration duration) {
		logger.debug("Visit " + duration.getClass().getName());	
	}

}
