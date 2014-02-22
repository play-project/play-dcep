package eu.play_project.querydispatcher.epsparql.tests.helpers;

import java.util.Stack;

import org.junit.Assert;

import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;

public class PostOrderTreeVisitor extends GenericVisitor {
	
	String[] expctedResults;
	int index;
	Stack<String> stack;
	
	public PostOrderTreeVisitor(String[] expctedResults) {
		this.expctedResults = expctedResults;
		stack = new Stack<String>();
		index = 0;
	}
	
	@Override
	public void visit(ElementEventBinOperator el) {
		el.getLeft().visit(this);
		el.getRight().visit(this);
		
		String right = stack.pop();
		String left = stack.pop();
		Assert.assertEquals(expctedResults[index++], left);
		Assert.assertEquals(expctedResults[index++], right);
		Assert.assertEquals(expctedResults[index++], el.getTyp());
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
		stack.push(el.getPattern().get(0).getSubject().toString());
	}
}
