package eu.play_project.querydispatcher.bdpl.tests.helpers;

import org.junit.Assert;

import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;

/**
 * Visit event binary operators and events in in-order fashion.
 * Compare string representation of the visited node with expected value.
 * @author sobermeier
 *
 */
public class SimpleEvenTreeVisitor extends GenericVisitor {
	
	String[] expctedResults;
	int index;
	
	public SimpleEvenTreeVisitor(String[] expctedResults) {
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
	public void visit(ElementEventGraph el) {
		el.getElement().visit(this);
	}
	
	@Override
	public void visit(ElementGroup el) {
		el.getElements().get(0).visit(this);
	}
	
	@Override
	public void visit(ElementPathBlock el) {
		Assert.assertEquals(expctedResults[index++], el.getPattern().get(0).getSubject().toString());
	}
}
