package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.CollectVariablesInTriplesAndFilterVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventMembersFromStream;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventPatternOperatorCollector;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.FilterExpressionCodeGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.HavingVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;
import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;
import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.unquoteFromProlog;


/**
 * Generate ELE code for complex part of a ETALIS pattern from a BDPL Jena tree.
 * @author sobermeier
 *
 */
public class ComplexPartEleGenerator {
	
	public String generateCode(Query query) {
		String ele = "";
		
		ele += Complex(query);
		//ele += "'http://events.event-processing.org/types/NoBiddersAlert'(abc, cba)";
		return ele;
	}
	
	private String Complex(Query inputQuery) {
		UniqueNameManager uniqueNameManager = getVarNameManager();
		String elePattern = "";
		
		// Detect complex type;
		elePattern += (new ComplexTypeFinder()).visit(inputQuery.getConstructTemplate());
		elePattern += "(" + uniqueNameManager.getNextCeid() + "," + inputQuery.getQueryId() + ") do (";
		
		// Collect complex event data.
		elePattern += GenerateConstructResult(inputQuery, inputQuery.getQueryId());
		elePattern += Having(inputQuery);
		elePattern += DecrementReferenceCounter();
		elePattern += ", constructResultIsNotEmpty(" + getVarNameManager().getCeid() + ")";
		getVarNameManager().resetTriplestoreVariable();
		elePattern += ")";
		
		return elePattern;
	}
	
	private String GenerateConstructResult(Query inputQuery, String patternId) {
		String constructResult = "";
		
		GenerateConstructResultVisitor generateConstructResultVisitor = new GenerateConstructResultVisitor();
		Iterator<Triple> constructTemplIter = inputQuery.getConstructTemplate().getTriples().iterator();
		Triple triple;
		
		// Concatenate q1, q2, q3 ...
		StringBuffer queriesConcatenated = new StringBuffer();
		for (String q : AllRdfQueryDbMethodDecl(inputQuery, patternId)) {
			if(queriesConcatenated.length() > 0){
				queriesConcatenated.append(", ");
				queriesConcatenated.append(q);
			}else{
				queriesConcatenated.append(q);
			}
		}


		constructResult += "" +
				"forall((" + queriesConcatenated.toString() + "), " +
									"(";
										//Filter
										constructResult += FilterExpression(inputQuery); // Add data only if filter matches.
										if(!constructResult.endsWith("(")) {
											constructResult += ", ";		// Connect filter and construct template.
										}
										//Generate code for construct result.
										while (constructTemplIter.hasNext()) {
											triple = constructTemplIter.next();
											if (!containsSharedVariablesTest(triple,inputQuery)) {
												constructResult += "" +
												"generateConstructResult("
														+ triple.getSubject().visitWith(
																generateConstructResultVisitor)
														+ ","
														+ triple.getPredicate().visitWith(
																generateConstructResultVisitor)
														+ ","
														+ triple.getObject().visitWith(
																generateConstructResultVisitor) + ","
														+ getVarNameManager().getCeid() +
													")";
												
												if (constructTemplIter.hasNext()) {
													constructResult += ", ";
												}
											}
										}
										constructResult += MemberEvents(inputQuery);
										constructResult += SaveSharedVariableValues(inputQuery);
				constructResult += ")";
		constructResult += ")";
		return constructResult.toString();
	}
	
	/**
	 * Generate rdf db method declarations.
	 * E.g. dbQuery_patternId_e1(Ve1).
	 * @param q Parsed Jena query.
	 * @return List of all method declarations to get sem web data.
	 */
	private List<String> AllRdfQueryDbMethodDecl(Query q, String patternId) {
		List<String> dbDecl = new LinkedList<String>();
		EventPatternOperatorCollector v = new EventPatternOperatorCollector();
		
		// Get all event clauses.
		v.collectValues(q.getEventQuery());
		for (Element currentElement : v.getEventPatterns()) {
			getVarNameManager().processNextEvent();
			dbDecl.add(RdfQueryDbMethodDecl(currentElement, getVarNameManager().getCurrentSimpleEventNumber(), patternId).toString());
		}
		return dbDecl;
	}
	
	public StringBuffer RdfQueryDbMethodDecl(Element currentElement, long l, String patternId) {
		// Get variables
		CollectVariablesInTriplesAndFilterVisitor v = new CollectVariablesInTriplesAndFilterVisitor();
		currentElement.visit(v);

		// Generate query method
		StringBuffer dbQueryDecl = new StringBuffer();

		// Schema "dbQuery" + patternId + idForEvent
		dbQueryDecl.append("'dbQuery_" + unquoteFromProlog(patternId) + "_e" + l + "'(");
		dbQueryDecl.append(getVarNameManager().getTriplestoreVariable()); // Mapping
																			     // between
																			     // event
																			     // and
																			     // corresponding
																			     // data.
		Iterator<String> iter = v.getVariables().iterator();
		while (iter.hasNext()) {
			dbQueryDecl.append(", " + iter.next());
			// Decide if it is the last variable or end of decl.
			if (!iter.hasNext()) {
				dbQueryDecl.append(")");
			}
		}

		return dbQueryDecl;
	}
	
	private String FilterExpression(Query q) {
		FilterExpressionCodeGenerator filterExpressionVisitor = new FilterExpressionCodeGenerator();
		String filterExp = "";
		
		EventPatternOperatorCollector v = new EventPatternOperatorCollector();
		v.collectValues(q.getEventQuery());
		// FIXME find Filter in triples.
		for (Element currentElement : v.getEventPatterns()) {
			filterExpressionVisitor.startVisit(currentElement);
			if(filterExpressionVisitor.getEle().length() > 1) {
				if(filterExp.length() > 1) {
					filterExp += ", " + filterExpressionVisitor.getEle();
				} else {
					filterExp += filterExpressionVisitor.getEle();
				}
			}
		}
		return filterExp;
	}
	
	private boolean containsSharedVariablesTest(Triple triple, Query inQuery){
		VariableTypeManager vtm = new VariableTypeManager(inQuery);
		
		boolean result = false;
		if(triple.getSubject().isVariable()){
			if(vtm.isType(triple.getSubject().getName(), VariableTypes.REALTIME_TYPE) && vtm.isType(triple.getSubject().getName(), VariableTypes.HISTORIC_TYPE)) {
				return true;
			}
			
		}
		if(triple.getPredicate().isVariable()) {
			if(vtm.isType(triple.getPredicate().getName(), VariableTypes.REALTIME_TYPE) && vtm.isType(triple.getPredicate().getName(), VariableTypes.HISTORIC_TYPE)){
				return true;
			}
		}
		if(triple.getObject().isVariable()) {
			if(vtm.isType(triple.getObject().getName(), VariableTypes.REALTIME_TYPE) && vtm.isType(triple.getObject().getName(), VariableTypes.HISTORIC_TYPE)){
				return true;
			}
		}
		return result;
	}
	
	private String MemberEvents(Query inputQuery) {
		String code = "";
		String staticCode = ", generateConstructResult('http://events.event-processing.org/types/e','http://events.event-processing.org/types/members'";
		
		EventMembersFromStream members = new EventMembersFromStream();

		for (String var : members.getMembersRepresentative(inputQuery)) {
			code += staticCode + "," + var + ", " + getVarNameManager().getCeid() + ")";
		}
		return code; 
	}
	
		
	public String SaveSharedVariableValues(Query q) {
		VariableTypeManager vtm = new VariableTypeManager(q);
		String elePattern = "";
		List<String> vars = vtm.getIntersection(VariableTypes.HISTORIC_TYPE, VariableTypes.REALTIME_TYPE);
		
		Iterator<String> iter = vars.iterator();
		String var;
		
		while (iter.hasNext()) {
			if (!elePattern.endsWith(",")) {
				elePattern += ",";
			}
			
			var= iter.next();
			elePattern += "variabeValuesAdd(" + getVarNameManager().getCeid() + "," + quoteForProlog(var) + "," + "V" + var + ")";
			
			if(iter.hasNext()){
				elePattern += ",";
			}
		}

		return elePattern;
	}
	
	private String DecrementReferenceCounter(){
		StringBuffer tmp = new StringBuffer();
		
		for (String var : getVarNameManager().getAllTripleStoreVariablesOfThisQuery()) {
			tmp.append(", decrementReferenceCounter("+ var + ")");
		}
		
		return tmp.toString();
	}
	
	public String Having(Query inputQuery){
		HavingVisitor havingVisitor =  new HavingVisitor();
		String elePattern = "";

		if(!inputQuery.getHavingExprs().isEmpty()){
			elePattern += ", ";
		}
		
		for (Expr el : inputQuery.getHavingExprs()) {
			el.visit(havingVisitor);
		}
		
		elePattern += havingVisitor.getCode().toString();
		
		return elePattern;
	}


}
