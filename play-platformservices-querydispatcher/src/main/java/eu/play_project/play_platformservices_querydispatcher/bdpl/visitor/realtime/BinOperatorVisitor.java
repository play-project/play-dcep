package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;


import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;

/**
 * Generate code for BDPL binary operators like SEQ, AND, OR ...
 * @author sobermeier
 *
 */
public class BinOperatorVisitor extends GenericVisitor implements ElementVisitor{
	String binOperator;
	
	public String getBinOperator(){
		return binOperator;
	}


	@Override
	public void visit(ElementEventBinOperator el) {
		binOperator = "'" + el.getTyp() + "'";
	}
}
