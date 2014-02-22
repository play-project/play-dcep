package eu.play_project.querydispatcher.epsparql.tests.helpers;

import org.junit.Assert;

import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;

public class SimpleEvenTreeVisitor extends GenericVisitor {
	
	String[] expctedResults;
	int index = 0;
	
	public SimpleEvenTreeVisitor(String[] expctedResults) {
		this.expctedResults = expctedResults;
		index = 0;
	}
	
	@Override
	public void visit(ElementEventBinOperator el) {
		el.getLeft().visit(this);
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
