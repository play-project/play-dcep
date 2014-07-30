package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.unquoteFromProlog;
import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;

import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.data_structure.EleEventPattern;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.CollectVariablesInTriplesAndFilterVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EqualizeEventIdVariableWithTriplestoreId;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventTypeVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.NotOperatorEleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.RdfQueryRepresentativeQueryVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.TriplestoreQueryVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;

/**
 * Generate ELE representation of BDPL event pattern.
 * @author sobermeier
 *
 */
public class EventPatternEleGenerator {
	
	Element eventGraph;
	String patternId;
	String rdfDbQuerie;
	VariableTypeManager varTypeManger;
	String executeCode; // Code will be executed in where clause.
	List<String> virtualPatterns;
	
	
	public EleEventPattern generateEle(Element eventGraph, String patternId, VariableTypeManager varTypeManger, String executeCode) {
		EleEventPattern pattern = new EleEventPattern();
		virtualPatterns = new ArrayList<String>();
		String ele = "";
		this.patternId = patternId;
		this.eventGraph = eventGraph;
		this.varTypeManger = varTypeManger;
		this.executeCode = executeCode;

		if (eventGraph instanceof ElementNotOperator) {
			NotOperatorEleGenerator notOperatorEleGenerator = new NotOperatorEleGenerator(varTypeManger, patternId, executeCode);
			eventGraph.visit(notOperatorEleGenerator);
			pattern = new EleEventPattern();
			String code = notOperatorEleGenerator.getEle();
			pattern.setMethodName(code);
			pattern.setMethodImpl(notOperatorEleGenerator.getMethodImpl());
			if (! notOperatorEleGenerator.getMethodImpl().isEmpty()) {
				pattern.setTriggerCode(notOperatorEleGenerator.getTriggerPattern());
			}
			virtualPatterns.add(pattern.getTriggerCode());
		} else {
			ele += SimpleEventPattern(eventGraph, executeCode);
		
			pattern.setMethodName(ele);
			List<String> tmp = new LinkedList<String>();
			tmp.add(rdfDbQuerie);
			pattern.setMethodImpl(tmp);
		}
		return pattern;
	}
	
	public String SimpleEventPattern(Element element, String executeCode) {
		String elePattern = "";
		EventTypeVisitor eventTypeVisitor = new EventTypeVisitor();
		UniqueNameManager uniqueNameManager = getVarNameManager();
	
		elePattern += "(";
		element.visit(eventTypeVisitor);
		elePattern += eventTypeVisitor.getEventType();
		elePattern += "(";
		elePattern += uniqueNameManager.getTriplestoreVariable();
		elePattern += ") 'WHERE' (";
		elePattern += AdditionalConditions(element);
		elePattern += executeCode + "))";
		
		return elePattern;
	}
	
	protected String AdditionalConditions(Element element){
		String elePattern = "";
		elePattern += TriplestoreQuery(element);
		elePattern += EventIdVarIsSynonymousWithTriplestoreId(element);
		elePattern += ReferenceCounter();
		
		return elePattern;
	}
	
	protected String EventIdVarIsSynonymousWithTriplestoreId(Element currentElement) {
		String ele = "";
		EqualizeEventIdVariableWithTriplestoreId eCcodeGeneratorVisitor = new EqualizeEventIdVariableWithTriplestoreId(getVarNameManager());
		currentElement.visit(eCcodeGeneratorVisitor);
		ele += eCcodeGeneratorVisitor.getEqualizeCode();
		
		return ele;
	}
	
	private String ReferenceCounter(){
		String elePattern = " incrementReferenceCounter(" + getVarNameManager().getTriplestoreVariable() + ")";
		
		return elePattern;
	}
	
	protected String TriplestoreQuery(Element currentElement) {
		UniqueNameManager uniqueNameManager = getVarNameManager();
		TriplestoreQueryVisitor triplestoreQueryVisitor = new TriplestoreQueryVisitor(uniqueNameManager, varTypeManger);
		String rdfDbQueries = "";
		String flatDbQueries;
		String ele = "";
		
		// Get flat queries
		currentElement.visit(triplestoreQueryVisitor);
		flatDbQueries = triplestoreQueryVisitor.getTriplestoreQueryGraphTerms();
		
		// Generate representative.
		RdfQueryRepresentativeQueryVisitor v = new RdfQueryRepresentativeQueryVisitor();
		currentElement.visit(v);

		//Generate query method
		StringBuffer dbQueryMethod = new StringBuffer();
		String dbQueryDecl = RdfQueryDbMethodDecl(currentElement, uniqueNameManager.getCurrentSimpleEventNumber()).toString();
		
		StringBuffer ignoreQueryIfEvntIdIsNotGiven = new StringBuffer();
		ignoreQueryIfEvntIdIsNotGiven.append("var(" + uniqueNameManager.getTriplestoreVariable() + ") -> true; "); // This means that this event was not consumed (event was optional).
		
		// Combine decl and impel.
		dbQueryMethod.append(dbQueryDecl + ":-(" + ignoreQueryIfEvntIdIsNotGiven + "(" + flatDbQueries + "))");
		rdfDbQueries = dbQueryMethod.toString();
		
		//Generate call for query.
		int i = 0;
		for (String key : v.getRdfQueryRepresentativeQuery().keySet()) {

			ele += "(\\+forall("
					+ v.getRdfQueryRepresentativeQuery().get(key) + ", "
					+ "(\\+((" + dbQueryDecl + ")))" + "))";

			if ((i < v.getRdfQueryRepresentativeQuery().size())) {
				ele += ",";
			}
		}
		this.rdfDbQuerie = rdfDbQueries;
		return ele;
	}
	
	public StringBuffer RdfQueryDbMethodDecl(Element currentElement, long l) {
		// Get variables
		CollectVariablesInTriplesAndFilterVisitor v = new CollectVariablesInTriplesAndFilterVisitor();
		currentElement.visit(v);

		// Generate query method
		StringBuffer dbQueryDecl = new StringBuffer();

		// Schema "dbQuery" + patternId + idForEvent
		dbQueryDecl.append("'dbQuery_" + unquoteFromProlog(patternId) + "_e" + l + "'(");
		dbQueryDecl.append(getVarNameManager().getTriplestoreVariable() + ", "); // Mapping
																			     // between
																			     // event
																			     // and
																			     // corresponding
																			     // data.
		Iterator<String> iter = v.getVariables().iterator();
		while (iter.hasNext()) {
			dbQueryDecl.append(iter.next());
			// Decide if it is the last variable or end of decl.
			if (iter.hasNext()) {
				dbQueryDecl.append(", ");
			} else {
				dbQueryDecl.append(")");
			}
		}

		return dbQueryDecl;
	}
}
