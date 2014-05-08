package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementFnAbsFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.RelationalOperator;


/**
 * This visitor generates no code. The visited node name is given to info logger.
 * @author sobermeier
 *
 */
public   class GenereicFilterExprVisitor implements ExprVisitor, ElementVisitor,  NodeValueVisitor {
	Logger logger;
	StringBuffer ele;
	// Test rdf('0.01','0.01',_L1,l1),rdf('0.1','0.1',_L2,l2),rdf('0.01','0.01',_B1,b1),rdf('0.1','0.1',_B2,b2),minus(_L2,_L1, _FilterVar1), abs(_FilterVar1, _FilterVar2), less(_FilterVar2, 0.1), minus(_B2,_B1, _FilterVar4), abs(_FilterVar4, _FilterVar5), less(_FilterVar5, 0.5)

	public GenereicFilterExprVisitor(){
		logger = LoggerFactory.getLogger(GenereicFilterExprVisitor.class);
		ele = new StringBuffer();
	}
	@Override
	public void startVisit() {
		
	}

	@Override
	public void visit(ExprFunction0 func) {
		logger.info("GenericVisitor visit: " + func.getClass().getName());
		
	}

	@Override
	public void visit(ExprFunction1 func) {
		logger.info("GenericVisitor visit: " + func.getClass().getName());
		
	}

	@Override
	public void visit(ExprFunction2 func) {
		logger.info("GenericVisitor visit: " + func.getClass().getName());
		
	}

	@Override
	public void visit(ExprFunction3 func) {
		logger.info("GenericVisitor visit: " + func.getClass().getName());
		
	}

	@Override
	public void visit(ExprFunctionN func) {
		logger.info("GenericVisitor visit: " + func.getClass().getName());
		
	}

	@Override
	public void visit(ExprFunctionOp funcOp) {
		logger.info("GenericVisitor visit: " + funcOp.getClass().getName());
		
	}

	@Override
	public void visit(NodeValue nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		System.out.println(nv.toString());
		
	}

	@Override
	public void visit(ExprVar nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		
	}

	@Override
	public void visit(ExprAggregator eAgg) {
		logger.info("GenericVisitor visit: " + eAgg.getClass().getName());
	}

	@Override
	public void finishVisit() {
		
	}
	@Override
	public void visit(RelationalOperator relationalOperator) {
		logger.info("GenericVisitor visit: " + relationalOperator.getClass().getName());
		
	}
	@Override
	public void visit(NodeValueBoolean nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		
	}

	@Override
	public void visit(NodeValueDouble nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		
	}
	@Override
	public void visit(NodeValueFloat nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		
	}
	@Override
	public void visit(NodeValueInteger nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		
	}
	@Override
	public void visit(NodeValueNode nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		
	}
	@Override
	public void visit(NodeValueString nv) {
		logger.info("GenericVisitor visit: " + nv.getClass().getName());
		
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementPathBlock el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementFilter el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementAssign el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementBind el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementUnion el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementOptional el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementGroup el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementDataset el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementExists el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementNotExists el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementService el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementSubQuery el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementEventGraph el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	@Override
	public void visit(ElementEventBinOperator el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}

	@Override
	public void visit(ElementNamedGraph el) {
		logger.info("GenericVisitor visit: " + el.getClass().getName());
		
	}
	
	@Override
	public void visit(ElementFnAbsFilter elementFnAbsFilter) {
		logger.info("GenericVisitor visit: " + elementFnAbsFilter.getClass().getName());
	}


	@Override
	public void visit(NodeValueDecimal arg0) {
		logger.info("GenericVisitor visit: " + arg0.getClass().getName());
		
	}
	@Override
	public void visit(NodeValueDT arg0) {
		logger.info("GenericVisitor visit: " + arg0.getClass().getName());
		
	}
	@Override
	public void visit(NodeValueDuration arg0) {
		logger.info("GenericVisitor visit: " + arg0.getClass().getName());
		
	}

	@Override
	public void visit(ElementData arg0) {
		logger.info("GenericVisitor visit: " + arg0.getClass().getName());
		
	}
	@Override
	public void visit(ElementMinus arg0) {
		logger.info("GenericVisitor visit: " + arg0.getClass().getName());
		
	}
	
}
