package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;

/**
 * Generate ELE for not operator.
 * @author sobermeier
 *
 */
public class NotOperatorEleGenerator extends GenericVisitor {
	String ele;
	
	public NotOperatorEleGenerator() {
		ele = "";
	}
	
	/**
	 * Transform "NOT (Start, Not, End)" to "(Start 'SEQ' End) 'CNOT' (Not)"
	 */
	@Override
	public void visit(ElementNotOperator elementNotOperator) {
		StringBuffer code = new StringBuffer();
	

		code.append("(") ;
		elementNotOperator.getStart().visit(this);
		code.append("'SEQ'");
		elementNotOperator.getEnd().visit(this);
		code.append(") 'cnot' (");
		elementNotOperator.getNot().visit(this);
		code.append(")");
		
		ele = code.toString();
	}
	
	private String ff() {
		
		
		while(binOperatorIter.hasNext()){
			elePattern += binOperatorIter.next();
			SimpleEventPattern();
		}
	}
	
	private void SimpleEventPattern(StringBuffer ele) {
		uniqueNameManager.processNextEvent();
	
		ele.append("(");
		currentElement = eventQueryIter.next();
		currentElement.visit(eventTypeVisitor);
		elePattern += eventTypeVisitor.getEventType();
		elePattern += "(";
		elePattern += uniqueNameManager.getTriplestoreVariable();
		elePattern += ") 'WHERE' (";
		AdditionalConditions();
		elePattern += "))";
	}
	
	
	
	public String getEle() {
		return this.ele;
	}
	

}
