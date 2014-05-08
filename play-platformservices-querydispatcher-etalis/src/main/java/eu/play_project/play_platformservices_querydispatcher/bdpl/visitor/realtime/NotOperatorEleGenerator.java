package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;

import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleEventPattern;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EventPatternEleGenerator;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;

/**
 * Generate ELE for not operator.
 * @author sobermeier
 *
 */
public class NotOperatorEleGenerator extends GenericVisitor {
	String executeCode; // Code will be executed after end event appeared. E.g. to generate complex event id.
	List<String> methodImpl;
	VariableTypeManager vtm;
	String patternId;
	String ele;
	
	public NotOperatorEleGenerator(VariableTypeManager vtm, String patternId, String executeCode) {
		this.executeCode = executeCode;
		this.methodImpl = new LinkedList<String>();
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
		EleEventPattern pattern;
		
		code.append("(") ;
		pattern = codeGenerator.generateEle(elementNotOperator.getStart(), patternId, vtm, "");
		methodImpl.addAll(pattern.getMethodImpl());
		code.append(pattern.getMethodName());
		code.append("'SEQ'");
		getVarNameManager().processNextEvent();
		pattern = codeGenerator.generateEle(elementNotOperator.getEnd(), patternId, vtm, executeCode);
		methodImpl.addAll(pattern.getMethodImpl());
		code.append(pattern.getMethodName());
		code.append(") 'cnot' (");
		getVarNameManager().processNextEvent();
		pattern = codeGenerator.generateEle(elementNotOperator.getNot(), patternId, vtm, "");
		methodImpl.addAll(pattern.getMethodImpl());
		code.append(pattern.getMethodName());
		code.append(")");
		
		ele = code.toString();
	}

	public String getEle() {
		return this.ele;
	}
	
	public List<String> getMethodImpl() {
		return methodImpl;
	}

}
