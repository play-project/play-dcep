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
	private Query inputQuery;
	// Contains the generated code.
	private String elePattern;
	// Currently selected element in the tree.
	private Element currentElement = null;
	// Manages variables which are globally unique.
	private UniqueNameManager uniqueNameManager;
	// Returns one triple after the other.
	private Iterator<ElementEventGraph> eventQueryIter;
	// Returns one operator after the other.
	private Iterator<String> binOperatorIter;
	// Detect the type of an event.
	private EventTypeVisitor eventTypeVisitor;
	//Visitors to generate code.
	private FilterExpressionCodeGenerator filterExpressionVisitor;
	private HavingVisitor havingVisitor;
	private TriplestoreQueryVisitor triplestoreQueryVisitor;
	private EqualizeEventIdVariableWithTriplestoreId eCcodeGeneratorVisitor;
	private CountEventsVisitor eventCounter;
	private VariableTypeManager nameManager;
	
	private List<String> rdfDbQueries; // Rdf db queries represents the semantic web part of a BDPL query. ETALIS calls this queries to check conditions for the current events.
	
	private String patternId;
	
	//Helper methods.
	private QueryTemplate queryTemplate;

	@Override
	public void generateQuery(Query inQuery) {
		//Detect event types
		//variableAgregatedType = new AgregatedVariableTypes().detectType(inQuery);

		elePattern = "";
		this.inputQuery = inQuery;
		uniqueNameManager = getVarNameManager();
		
		uniqueNameManager.setWindowTime(inQuery.getWindow().getValue());
		
		// Instantiate visitors.
		EventPatternOperatorCollector eventPatternVisitor =  new EventPatternOperatorCollector();
		eventTypeVisitor = new EventTypeVisitor();
		triplestoreQueryVisitor = new TriplestoreQueryVisitor(uniqueNameManager);
		filterExpressionVisitor = new FilterExpressionCodeGenerator();
		eCcodeGeneratorVisitor = new EqualizeEventIdVariableWithTriplestoreId(uniqueNameManager);
		eventCounter =  new CountEventsVisitor();
		havingVisitor =  new HavingVisitor();
		
		// Collect basic informations like number of events or variable types.
		uniqueNameManager.newQuery(eventCounter.count(inQuery.getEventQuery()));
		
		queryTemplate = new QueryTemplateImpl();
		UniqueNameManager.initVariableTypeManager(inQuery);
		nameManager =  UniqueNameManager.getVariableTypeManage();
		nameManager.collectVars();

		// Collect event patterns and operators.
		eventPatternVisitor.collectValues(inQuery.getEventQuery());
		eventQueryIter = eventPatternVisitor.getEventPatterns().iterator();
		binOperatorIter = eventPatternVisitor.getOperators().iterator();
		
		rdfDbQueries = new LinkedList<String>();
		
		// Start code generation.
		ElePattern();
	}
	
	private void ElePattern(){
		
		Complex();
		elePattern += "<-";
		SimpleEventPattern();
		
		while(binOperatorIter.hasNext()){
			elePattern += binOperatorIter.next();
			SimpleEventPattern();
		}
	}

	private void Complex() {
		//Detect complex type;
		elePattern += (new ComplexTypeFinder()).visit(inputQuery.getConstructTemplate());
		elePattern += "(" + uniqueNameManager.getNextCeid() + "," + patternId + ") do (";
		GenerateConstructResult();
		Having();
		//PrintStatisticsData();
		DecrementReferenceCounter();
		elePattern += ", constructResultIsNotEmpty(" + getVarNameManager().getCeid() + ")";
		getVarNameManager().resetTriplestoreVariable();
		elePattern += ")";
	}

	private void GenerateConstructResult() {
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
											if (!containsSharedVariablesTest(triple)) {
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
														+ uniqueNameManager.getCeid() +
													")";
												
												if (constructTemplIter.hasNext()) {
													constructResult += ", ";
												}
											}
										}
										constructResult += MemberEvents();
										constructResult += SaveSharedVariableValues();
				constructResult += ")";
		constructResult += ")";
		elePattern += constructResult.toString();
	}

	private String MemberEvents() {
		String code = "";
		String staticCode = ", generateConstructResult('http://events.event-processing.org/types/e','http://events.event-processing.org/types/members'";
		
		EventMembersFromStream members = new EventMembersFromStream();

		for (String var : members.getMembersRepresentative(inputQuery)) {
			code += staticCode + "," + var + ", " + uniqueNameManager.getCeid() + ")";
		}
		return code; 
	}
	
	private boolean containsSharedVariablesTest(Triple triple){
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
		List<String> vars = nameManager.getIntersection(VariableTypes.HISTORIC_TYPE, VariableTypes.REALTIME_TYPE);
		
		Iterator<String> iter = vars.iterator();
		String var;
		
		while (iter.hasNext()) {
			if (!elePattern.endsWith(",")) {
				elePattern += ",";
			}
			
			var= iter.next();
			elePattern += "variabeValuesAdd(" + uniqueNameManager.getCeid() + "," + quoteForProlog(var) + "," + "V" + var + ")";
			
			if(iter.hasNext()){
				elePattern += ",";
			}
		}

		return elePattern;
	}
	
	private void DecrementReferenceCounter(){
		StringBuffer tmp = new StringBuffer();
		
		for (String var : uniqueNameManager.getAllTripleStoreVariablesOfThisQuery()) {
			tmp.append(", decrementReferenceCounter("+ var + ")");
		}
		
		elePattern += tmp.toString();
	}
	
	//Call prolog methods which echos statistics data to the console.
	private void PrintStatisticsData(){
		elePattern += ", printRdfStat, printRefCountN";
	}
	
	private void SimpleEventPattern() {
		uniqueNameManager.processNextEvent();
	
		elePattern += "(";
		currentElement = eventQueryIter.next();
		currentElement.visit(eventTypeVisitor);
		elePattern += eventTypeVisitor.getEventType();
		elePattern += "(";
		elePattern += uniqueNameManager.getTriplestoreVariable();
		elePattern += ") 'WHERE' (";
		AdditionalConditions();
		elePattern += "))";
	}
	
	private void AdditionalConditions(){
		TriplestoreQuery();
		EventIdVarIsSynonymousWithTriplestoreId();
		ReferenceCounter();
//		elePattern += ", ";
//		PerformanceMeasurement();
		
		if(!binOperatorIter.hasNext()){
			elePattern += ",";
			GenerateCEID();
		}
	}
	
	private void EventIdVarIsSynonymousWithTriplestoreId() {
		currentElement.visit(eCcodeGeneratorVisitor);
		elePattern += eCcodeGeneratorVisitor.getEqualizeCode();
	}

	private void ReferenceCounter(){
		elePattern += " incrementReferenceCounter(" + uniqueNameManager.getTriplestoreVariable() + ")";
	}
	
	private void PerformanceMeasurement(){
		elePattern += "measure(" +  patternId + ")";
	}
	
	
	private void TriplestoreQuery() {
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

			elePattern += "(\\+forall("
					+ v.getRdfQueryRepresentativeQuery().get(key) + ", "
					+ "(\\+((" + dbQueryDecl + ")))" + "))";

			if ((i < v.getRdfQueryRepresentativeQuery().size())) {
				elePattern += ",";
			}
		}
	}

	private String FilterExpression(Query q) {
		String filterExp = "";
		
		EventPatternOperatorCollector v = new EventPatternOperatorCollector();
		v.collectValues(q.getEventQuery());
		
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
		
	private void GenerateCEID(){
		elePattern += "random(1000000, 9000000, " + uniqueNameManager.getCeid() + ")";
	}

	@Override
	public String getEle() {
		return elePattern;
	}

	@Override
	public ArrayList<String[]> getEventProperties() {
		return null;
	}
	
	@Override
	public void setPatternId(String patternId) {
		this.patternId = quoteForProlog(patternId);
	}

	@Override
	public QueryTemplate getQueryTemplate() {
		return this.queryTemplate;
	}
	
	
	public void Having(){
		
		if(!inputQuery.getHavingExprs().isEmpty()){
			elePattern += ", ";
		}
		
		for (Expr el : inputQuery.getHavingExprs()) {
			el.visit(havingVisitor);
		}
		
		elePattern += havingVisitor.getCode().toString();
	}

	@Override
	public List<String> getRdfDbQueries() {
		return rdfDbQueries;
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
			dbDecl.add(RdfQueryDbMethodDecl(currentElement, uniqueNameManager.getCurrentSimpleEventNumber()).toString());
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
}