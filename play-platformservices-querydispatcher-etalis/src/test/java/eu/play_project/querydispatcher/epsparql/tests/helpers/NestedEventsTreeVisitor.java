package eu.play_project.querydispatcher.epsparql.tests.helpers;

import org.junit.Assert;

import com.hp.hpl.jena.sparql.syntax.ElementBraceOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;

/**
 * Visit tree elements to generate ELE and compare the visited nodes with with expected values.
 * Visit tree in in-order  fashion and add brackets. 
 * @author Stefan Obermeier
 *
 */
public class NestedEventsTreeVisitor extends GenericVisitor {
	
	String[] expctedResults;
	int index;
	
	public NestedEventsTreeVisitor(String[] expctedResults) {
		this.expctedResults = expctedResults;
		index = 0;
	}
	
	@Override
	public void visit(ElementEventBinOperator el) {
		el.getLeft().visit(this);
		Assert.assertEquals(expctedResults[index++], el.getTyp());
		el.getRight().visit(this);
	}
	
	@Override
	public void visit(ElementBraceOperator el) {
		Assert.assertEquals("(", expctedResults[index++]);
		el.getSubElements().visit(this);
		Assert.assertEquals(")", expctedResults[index++]);
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		el.getElement().visit(this);
	}
	
	@Override
	public void visit(ElementGroup el) {
		el.getElements().get(0).visit(this);
	}
	
	@Override
	public void visit(ElementPathBlock el) {
		Assert.assertEquals(el.getPattern().get(0).getSubject().toString(), expctedResults[index++]);
	}
}
