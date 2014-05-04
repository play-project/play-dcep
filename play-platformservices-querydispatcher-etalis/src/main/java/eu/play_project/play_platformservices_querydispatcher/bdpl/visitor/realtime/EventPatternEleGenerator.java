package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.unquoteFromProlog;
import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import java.util.Iterator;
import java.util.LinkedList;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.syntax.Element;

import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;

/**
 * Generate ele representation of BDPL event pattern.
 * @author sobermeier
 *
 */
public class EventPatternEleGenerator {
	
	Element eventGraph;
	String patternId;
	LinkedList<String> rdfDbQueries;
	VariableTypeManager varTypeManger;
	
	
	public EleEventPattern generateEle(Element eventGraph, String patternId, VariableTypeManager varTypeManger) {
		EleEventPattern pattern = new EleEventPattern();
		String ele = "";
		this.patternId = patternId;
		this.eventGraph = eventGraph;
		this.varTypeManger = varTypeManger;
		
		ele += SimpleEventPattern(eventGraph);
		
		pattern.setMethodName(ele);
		pattern.setMethodImpl(rdfDbQueries);
		return pattern;
	}
	
	public String SimpleEventPattern(Element element) {
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
		
//		if(!binOperatorIter.hasNext()){
//			elePattern += ",";
//			GenerateCEID(elePattern);
//		}
		elePattern += "))";
		
		return elePattern;
	}
	
	private String AdditionalConditions(Element element){
		String elePattern = "";
		elePattern += TriplestoreQuery(element);
		elePattern += EventIdVarIsSynonymousWithTriplestoreId(element);
		elePattern += ReferenceCounter();
		
		return elePattern;
	}
	
	private String EventIdVarIsSynonymousWithTriplestoreId(Element currentElement) {
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
	
	private String TriplestoreQuery(Element currentElement) {
		UniqueNameManager uniqueNameManager = getVarNameManager();
		TriplestoreQueryVisitor triplestoreQueryVisitor = new TriplestoreQueryVisitor(uniqueNameManager, varTypeManger);
		LinkedList<String> rdfDbQueries = new LinkedList<String>();
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
		rdfDbQueries.add(dbQueryMethod.toString());
		
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
		this.rdfDbQueries = rdfDbQueries;
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
