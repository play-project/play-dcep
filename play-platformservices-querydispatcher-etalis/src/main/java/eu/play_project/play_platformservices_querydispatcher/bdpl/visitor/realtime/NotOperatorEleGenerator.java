package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementDuration;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;

import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.data_structure.EleEventPattern;
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
	String triggerPattern;
	VariableTypeManager vtm;
	String patternId;
	String ele;
	
	public NotOperatorEleGenerator(VariableTypeManager vtm, String patternId, String executeCode) {
		this.executeCode = executeCode;
		this.methodImpl = new LinkedList<String>();
		this.triggerPattern = "";
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
		String startEvent = "";
		EventPatternEleGenerator codeGenerator =  new EventPatternEleGenerator();
		EleEventPattern pattern;
		
		code.append("(") ;
		pattern = codeGenerator.generateEle(elementNotOperator.getStart(), patternId, vtm, "");
		methodImpl.addAll(pattern.getMethodImpl());
		startEvent = pattern.getMethodName();
		code.append(pattern.getMethodName());
		code.append(" 'SEQ' ");
		
		getVarNameManager().processNextEvent();
		
		
		if (elementNotOperator.getEnd() instanceof ElementDuration) {
			// Set virtual event in main pattern.
			code.append(getVarNameManager().getVirtualEvent());
			
			// Generate code to fire virtual event.
			NotEventEleGenerator notEventEleGenerator = new NotEventEleGenerator();
			pattern =  notEventEleGenerator.generate(elementNotOperator.getEnd(), startEvent, getVarNameManager().getVirtualEvent());
			triggerPattern = pattern.getTriggerCode();
		} else {
			pattern = codeGenerator.generateEle(elementNotOperator.getEnd(), patternId, vtm, executeCode);
			methodImpl.addAll(pattern.getMethodImpl());
			code.append(pattern.getMethodName());
		}
		
		code.append(") 'cnot' (");
		getVarNameManager().processNextEvent();

		NotEventEleGenerator notEventEleGenerator = new NotEventEleGenerator();
		pattern =  notEventEleGenerator.generate(elementNotOperator.getNot(), startEvent, "");
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
	
	/**
	 * Generate code depending on not condition. 
	 * It can be a time or event based not operator.
	 * @author Stefan Obermeier
	 *
	 */
	class NotEventEleGenerator extends GenericVisitor {
		private EleEventPattern pattern;
		private String virtualEvent;
		private String startEvent;
		
		/**
		 *  Generate end node code.
		 * @param el Not operator end node.
		 * @param startEvent Simple event which will start the timer.
		 * @param virtualEvent Event which will be produced when time is up.
		 * @return
		 */
		public EleEventPattern generate(Element el, String startEvent, String virtualEvent) {
			pattern = new EleEventPattern();
			this.virtualEvent = virtualEvent;
			this.startEvent = startEvent;
			
			// For one event only.
			el.visit(this);
			
			return pattern;
		}
		
		@Override
		public void visit(ElementDuration duration) {
			pattern.setTriggerCode(("dc <- " + startEvent + " 'WHERE'(triggerEventWithDelay("+ virtualEvent + ", " + duration.getTimeInSeconds() + "))"));
		}
		
		@Override
		public void visit(ElementEventGraph event) {
			EventPatternEleGenerator codeGenerator =  new EventPatternEleGenerator();
			EleEventPattern patternLocal = codeGenerator.generateEle(event, patternId, vtm, "");
			pattern.setMethodImpl(patternLocal.getMethodImpl());
			pattern.setMethodName(patternLocal.getMethodName());
		}		
	}

	public String getTriggerPattern() {
		return this.triggerPattern;
	}

}
