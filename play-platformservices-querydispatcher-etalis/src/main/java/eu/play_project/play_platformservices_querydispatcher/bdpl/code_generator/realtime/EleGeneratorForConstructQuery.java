package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;
import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.unquoteFromProlog;
import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementCep;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.CollectVariablesInTriplesAndFilterVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.CountEventsVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EqualizeEventIdVariableWithTriplestoreId;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventMembersFromStream;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventPatternOperatorCollector;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventTypeVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.FilterExpressionCodeGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenerateConstructResultVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.HavingVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.RdfQueryRepresentativeQueryVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.TriplestoreQueryVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;


/**
 * This class coordinates the code generation for the CEP-Engine.
 * @author sobermeier
 *
 */
public class EleGeneratorForConstructQuery implements EleGenerator {
	private String patternId;
	// Returns one triple after the other.
	private Iterator<ElementCep> eventQueryIter;
	

	@Override
	public void generateQuery(Query inQuery) {
		String elePattern = "";
		Query inputQuery = inQuery;
		UniqueNameManager uniqueNameManager = getVarNameManager();
		
		uniqueNameManager.setWindowTime(inQuery.getWindow().getValue());
		
		// Instantiate visitors.
		EventTypeVisitor eventTypeVisitor = new EventTypeVisitor();
		
		
		CountEventsVisitor eventCounter =  new CountEventsVisitor();
		
		// Collect basic informations like number of events or variable types.
		uniqueNameManager.newQuery(eventCounter.count(inQuery.getEventQuery()));
		
		QueryTemplate queryTemplate = new QueryTemplateImpl();
		LinkedList<String> rdfDbQueries = new LinkedList<String>();
		
		// Start code generation.
		ElePattern(elePattern, inputQuery);
	}
	
	private void ElePattern(String elePattern, Query inputQuery){
		
		// Separate operators from patterns.
		EventPatternOperatorCollector eventPatternVisitor =  new EventPatternOperatorCollector();
		eventPatternVisitor.collectValues(inputQuery.getEventQuery());
		Iterator<String> binOperatorIter = eventPatternVisitor.getOperators().iterator();
		eventQueryIter = eventPatternVisitor.getEventPatterns().iterator();

		Complex(elePattern, patternId, inputQuery);
		elePattern += "<-";
		SimpleEventPattern(elePattern, eventQueryIter.next());
		
		while(binOperatorIter.hasNext()){
			elePattern += binOperatorIter.next();
			SimpleEventPattern(elePattern, eventQueryIter.next());
		}
	}

	private void Complex(String elePattern, String patternId, Query inputQuery) {
		UniqueNameManager uniqueNameManager = getVarNameManager();
		
		//Detect complex type;
		elePattern += (new ComplexTypeFinder()).visit(inputQuery.getConstructTemplate());
		elePattern += "(" + uniqueNameManager.getNextCeid() + "," + patternId + ") do (";
		GenerateConstructResult(elePattern, inputQuery);
		Having(inputQuery, elePattern);
		//PrintStatisticsData();
		DecrementReferenceCounter(elePattern);
		elePattern += ", constructResultIsNotEmpty(" + getVarNameManager().getCeid() + ")";
		getVarNameManager().resetTriplestoreVariable();
		elePattern += ")";
	}

	private void GenerateConstructResult(String ele, Query inputQuery) {
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
										constructResult += SaveSharedVariableValues();
				constructResult += ")";
		constructResult += ")";
		ele += constructResult.toString();
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
	
	private boolean containsSharedVariablesTest(Triple triple, Query inQuery){
		UniqueNameManager.initVariableTypeManager(inQuery);
		VariableTypeManager nameManager =  UniqueNameManager.getVariableTypeManage();
		nameManager.collectVars();
		
		boolean result = false;
		if(triple.getSubject().isVariable()){
			if(nameManager.isType(triple.getSubject().getName(), VariableTypes.REALTIME_TYPE) && nameManager.isType(triple.getSubject().getName(), VariableTypes.HISTORIC_TYPE)){
				return true;
			}
			
		}
		if(triple.getPredicate().isVariable()){
			if(nameManager.isType(triple.getPredicate().getName(), VariableTypes.REALTIME_TYPE) && nameManager.isType(triple.getPredicate().getName(), VariableTypes.HISTORIC_TYPE)){
				return true;
			}
		}
		if(triple.getObject().isVariable()){
			if(nameManager.isType(triple.getObject().getName(), VariableTypes.REALTIME_TYPE) && nameManager.isType(triple.getObject().getName(), VariableTypes.HISTORIC_TYPE)){
				return true;
			}
		}
		return result;
	}
		
	public String SaveSharedVariableValues() {
		String elePattern = "";
		List<String> vars = getVarNameManager().getVariableTypeManage().getIntersection(VariableTypes.HISTORIC_TYPE, VariableTypes.REALTIME_TYPE);
		
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
	
	private void DecrementReferenceCounter(String elePattern){
		StringBuffer tmp = new StringBuffer();
		
		for (String var : getVarNameManager().getAllTripleStoreVariablesOfThisQuery()) {
			tmp.append(", decrementReferenceCounter("+ var + ")");
		}
		
		elePattern += tmp.toString();
	}
	
	//Call prolog methods which echos statistics data to the console.
	private void PrintStatisticsData(String elePattern){
		elePattern += ", printRdfStat, printRefCountN";
	}
	
	public void SimpleEventPattern(String elePattern, Element element) {
		EventTypeVisitor eventTypeVisitor = new EventTypeVisitor();
		UniqueNameManager uniqueNameManager = getVarNameManager();
	
		elePattern += "(";
		element.visit(eventTypeVisitor);
		elePattern += eventTypeVisitor.getEventType();
		elePattern += "(";
		elePattern += uniqueNameManager.getTriplestoreVariable();
		elePattern += ") 'WHERE' (";
		AdditionalConditions(elePattern);
		elePattern += "))";
	}
	
	private void AdditionalConditions(String elePattern, Iterator<String> binOperatorIter){
		TriplestoreQuery(elePattern);
		EventIdVarIsSynonymousWithTriplestoreId(elePattern);
		ReferenceCounter(elePattern);
//		elePattern += ", ";
//		PerformanceMeasurement();
		
		if(!binOperatorIter.hasNext()){
			elePattern += ",";
			GenerateCEID(elePattern);
		}
	}
	
	private void EventIdVarIsSynonymousWithTriplestoreId(String ele, Element currentElement) {
		EqualizeEventIdVariableWithTriplestoreId eCcodeGeneratorVisitor = new EqualizeEventIdVariableWithTriplestoreId(getVarNameManager());
		currentElement.visit(eCcodeGeneratorVisitor);
		ele += eCcodeGeneratorVisitor.getEqualizeCode();
	}

	private void ReferenceCounter(String elePattern){
		elePattern += " incrementReferenceCounter(" + getVarNameManager().getTriplestoreVariable() + ")";
	}
	
	private void PerformanceMeasurement(String elePattern){
		elePattern += "measure(" +  patternId + ")";
	}
	
	
	private void TriplestoreQuery(String ele, Element currentElement, LinkedList<String> rdfDbQueries) {
		UniqueNameManager uniqueNameManager = getVarNameManager();
		TriplestoreQueryVisitor triplestoreQueryVisitor = new TriplestoreQueryVisitor(uniqueNameManager);
		String flatDbQueries;
		
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
	}

	private String FilterExpression(Query q) {
		FilterExpressionCodeGenerator filterExpressionVisitor = new FilterExpressionCodeGenerator();
		String filterExp = "";
		
		EventPatternOperatorCollector v = new EventPatternOperatorCollector();
		v.collectValues(q.getEventQuery());
		// FIXME find Filter in triples.
		for (Element currentElement : v.getEventPatterns()) {
			filterExpressionVisitor.startVisit(((ElementEventGraph)currentElement).getFilterExp());
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
		
	private void GenerateCEID(String elePattern){
		elePattern += "random(1000000, 9000000, " + getVarNameManager().getCeid() + ")";
	}

	@Override
	public ArrayList<String[]> getEventProperties() {
		return null;
	}
	
	@Override
	public void setPatternId(String patternId) {
		this.patternId = quoteForProlog(patternId);
	}

	public void Having(Query inputQuery, String elePattern){
		HavingVisitor havingVisitor =  new HavingVisitor();

		if(!inputQuery.getHavingExprs().isEmpty()){
			elePattern += ", ";
		}
		
		for (Expr el : inputQuery.getHavingExprs()) {
			el.visit(havingVisitor);
		}
		
		elePattern += havingVisitor.getCode().toString();
	}

	
	/**
	 * Generate rdf db method decelerations.
	 * E.g. dbQuery_abc_e1(Ve1).
	 * @param q Parsed Jena query.
	 * @return
	 */
	private List<String> AllRdfQueryDbMethodDecl(Query q, String patternId) {
		List<String> dbDecl = new LinkedList<String>();
		EventPatternOperatorCollector v = new EventPatternOperatorCollector();
		v.collectValues(q.getEventQuery());
		for (Element currentElement : v.getEventPatterns()) {
			getVarNameManager().processNextEvent();
			dbDecl.add(RdfQueryDbMethodDecl(currentElement, getVarNameManager().getCurrentSimpleEventNumber()).toString());
		}
		return dbDecl;
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
	
	@Override
	public List<String> getRdfDbQueries() {
		return rdfDbQueries;
	}
	
	@Override
	public QueryTemplate getQueryTemplate() {
		return this.queryTemplate;
	}
	
	@Override
	public String getEle() {
		return elePattern;
	}
}