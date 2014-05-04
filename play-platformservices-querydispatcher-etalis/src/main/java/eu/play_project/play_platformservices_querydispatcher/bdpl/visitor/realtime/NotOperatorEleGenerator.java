package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;

import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;

/**
 * Generate ELE for not operator.
 * @author sobermeier
 *
 */
public class NotOperatorEleGenerator extends GenericVisitor {
	VariableTypeManager vtm;
	String patternId;
	String ele;
	
	public NotOperatorEleGenerator(VariableTypeManager vtm, String patternId) {
		this.patternId = patternId;
		this.vtm = vtm;
		ele = "";
	}
	
	/**
	 * Transform "NOT(Start, Not, End)" to "(Start 'SEQ' End) 'CNOT' (Not)"
	 */
	@Override
	public void visit(ElementNotOperator elementNotOperator) {
		StringBuffer code = new StringBuffer();
		EventPatternEleGenerator codeGenerator =  new EventPatternEleGenerator();

		code.append("(") ;
		code.append(codeGenerator.generateEle(elementNotOperator.getStart(), patternId, vtm).getMethodName());
		code.append("'SEQ'");
		code.append(codeGenerator.generateEle(elementNotOperator.getEnd(), patternId, vtm).getMethodName());
		code.append(") 'cnot' (");
		code.append(codeGenerator.generateEle(elementNotOperator.getNot(), patternId, vtm).getMethodName());
		code.append(")");
		
		ele = code.toString();
	}

	public String getEle() {
		return this.ele;
	}
	

}
