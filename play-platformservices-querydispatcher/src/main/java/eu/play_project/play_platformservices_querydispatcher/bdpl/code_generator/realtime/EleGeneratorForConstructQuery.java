package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager.getVarNameManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.BinOperatorVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.CollectVariablesInTriplesVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
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
	private Iterator<Element> eventQueryIter;
	// Return operators used in the query. E.g. SEQ, AND, OR ... In the order they are used in the query.
	private Iterator<ElementEventBinOperator> binOperatorIter;
	// Detect the type of an event.
	private EventTypeVisitor eventTypeVisitor;
	//Visitors to generate code.
	private BinOperatorVisitor binOperatorVisitor;
	private FilterExpressionCodeGenerator filterExpressionVisitor;
	private HavingVisitor havingVisitor;
	private TriplestoreQueryVisitor triplestoreQueryVisitor;
	private VariableTypeManager nameManager;
	
	private List<String> rdfDbQueries; // Rdf db queries represents the semantic web part of a BDPL query. ETALIS calls this queries to check conditions for the current events.
	private int eventCounter; // Count number of events in a query.
	
	private String patternId;
	
	//Helper methods.
	private QueryTemplate queryTemplate;

	@Override
	public void generateQuery(Query inQuery) {
		//Detect eventtypes
		//variableAgregatedType = new AgregatedVariableTypes().detectType(inQuery);

		elePattern = "";
		this.inputQuery = inQuery;
		eventQueryIter = inQuery.getEventQuery().iterator();
		binOperatorIter = inQuery.getEventBinOperator().iterator();
		uniqueNameManager = getVarNameManager();
		uniqueNameManager.newQuery(); // Rest uniqueNameManager.
		uniqueNameManager.setWindowTime(inQuery.getWindow().getValue());
		// Instantiate visitors.
		eventTypeVisitor = new EventTypeVisitor();
		triplestoreQueryVisitor = new TriplestoreQueryVisitor(uniqueNameManager);
		filterExpressionVisitor = new FilterExpressionCodeGenerator();
		binOperatorVisitor =  new BinOperatorVisitor();
		havingVisitor =  new HavingVisitor();
		
		queryTemplate = new QueryTemplateImpl();
		
		// Collect basic informations like variable types.
		UniqueNameManager.initVariableTypeManage(inQuery);
		nameManager =  UniqueNameManager.getVariableTypeManage();
		nameManager.collectVars();

		rdfDbQueries = new LinkedList<String>();
		eventCounter = 0;
		// Start code generation.
		ElePattern();
	}
	
	private void ElePattern(){
		StringBuffer generateConstructCode;
		
		Complex();
		elePattern += "<-";
		SimpleEventPattern();
		
		while(binOperatorIter.hasNext()){
			binOperatorIter.next().visit(binOperatorVisitor);
			elePattern += binOperatorVisitor.getBinOperator();
			SimpleEventPattern();
		}
	}

	private void Complex() {
		//Detect complex type;
		elePattern += (new ComplexTypeFinder()).visit(inputQuery.getConstructTemplate());
		elePattern += "(" + uniqueNameManager.getNextCeid() + "," + patternId + ") do (";
		GenerateConstructResult();
		SaveSharedVariabelValues();
		Having();
		//PrintStatisticsData();
		DecrementReferenceCounter();
		elePattern += ")";
	}

	private void GenerateConstructResult() {
		StringBuffer constructResult = new StringBuffer();
		
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


		constructResult.append("" +
				"forall((" + queriesConcatenated.toString() + "), " +
									"(");
										while (constructTemplIter.hasNext()) {
											triple = constructTemplIter.next();
											if (!containsSharedVariablesTest(triple)) {
												constructResult.append( "" +
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
													") ");
												
												if (constructTemplIter.hasNext()) {
													constructResult.append(", ");
												}
											}
										}
				constructResult.append(")");
		constructResult.append(")");
		
		elePattern += constructResult.toString();
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
		
	public void SaveSharedVariabelValues() {
		StringBuffer tmpEle = new StringBuffer();

		List<String> vars = nameManager.getVariables(VariableTypes.HISTORIC_TYPE, VariableTypes.REALTIME_TYPE);
		for (String var : vars) {
			if (!elePattern.endsWith(",")) {
				elePattern += ",";
			}
			tmpEle.append("variabeValuesAdd(" + patternId + ",'" + var + "'," + "V" + var + ")");
		}
		elePattern += tmpEle.toString();
	}
	
	private void DecrementReferenceCounter(){
		StringBuffer tmp = new StringBuffer();
		
		for (String var : uniqueNameManager.getAllTripleStoreVariablesOfThisQuery()) {
			tmp.append(", decrementReferenceCounter( "+ var + ")");
		}
		
		elePattern += tmp.toString();
	}
	
	//Call prolog methods which echos statistics data to the console.
	private void PrintStatisticsData(){
		elePattern += ", printRdfStat, printRefCountN";
	}
	private void SimpleEventPattern() {
	
		elePattern += "(";
		currentElement = eventQueryIter.next();
		currentElement.visit(eventTypeVisitor);
		elePattern += eventTypeVisitor.getEventType();
		elePattern += "(";
		elePattern += uniqueNameManager.getNextTriplestoreVariable();
		elePattern += ") 'WHERE' (";
		AdditionalConditions();
		elePattern += "))";
	}
	
	private void AdditionalConditions(){
		TriplestoreQuery(FilterExpression());
		ReferenceCounter();
//		elePattern += ", ";
//		PerformanceMeasurement();
		// FIXME sobermeier: re-add these lines and test for PrologException on simple events
		
		if(!binOperatorIter.hasNext()){
			elePattern += ",";
			GenerateCEID();
		}
	}

	private void ReferenceCounter(){
		elePattern += " incrementReferenceCounter(" + uniqueNameManager.getTriplestoreVariable() + ")";
	}
	
	private void PerformanceMeasurement(){
		elePattern += "measure(" +  patternId + ")";
	}
	
	
	private void TriplestoreQuery(String filter) {
		String flatDbQueries;
		eventCounter++; // New event in query.
		
		// Get flat queries
		currentElement.visit(triplestoreQueryVisitor);
		flatDbQueries = triplestoreQueryVisitor.getTriplestoreQueryGraphTerms();
		if (filter.length() > 4) {
			flatDbQueries += ", " + filter.substring(3, filter.length()-2);
		}
		
		// Generate representative.
		RdfQueryRepresentativeQueryVisitor v = new RdfQueryRepresentativeQueryVisitor();
		currentElement.visit(v);

		//Generate query method
		StringBuffer dbQueryMethod = new StringBuffer();
		String dbQueryDecl = RdfQueryDbMethodDecl(currentElement, eventCounter).toString();
		
		// Combine decl and impl.
		dbQueryMethod.append(dbQueryDecl + ":-(" + flatDbQueries + ")");
		System.out.println("\n\n\n\n " + dbQueryDecl + "\n\n\n\n");

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

	private String FilterExpression() {
		filterExpressionVisitor.startVisit(((ElementEventGraph)currentElement).getFilterExp());
		if(!elePattern.endsWith(",") && !filterExpressionVisitor.getEle().equals("")){ // This filter is optional. No value needed.
			return "," + filterExpressionVisitor.getEle();
		}else if(!filterExpressionVisitor.getEle().equals("")){
			return filterExpressionVisitor.getEle().toString();
		}
		return "";
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
		this.patternId = "'" + patternId + "'";
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
	 * Generate rdf db method declerations.
	 * E.g. dbQuery_abc_e1(Ve1).
	 * @param q Parsed Jena query.
	 * @return
	 */
	private List<String> AllRdfQueryDbMethodDecl(Query q, String patternId) {
		List<String> dbDecl = new LinkedList<String>();
		
		int eventCounter = 0;
		for (Element currentElement : q.getEventQuery()) {
			eventCounter++;
			dbDecl.add(RdfQueryDbMethodDecl(currentElement, eventCounter).toString());
		}
		return dbDecl;
	}
	
	public StringBuffer RdfQueryDbMethodDecl(Element currentElement, int eventCounter) {
		// Get variables
		CollectVariablesInTriplesVisitor v = new CollectVariablesInTriplesVisitor();
		currentElement.visit(v);

		// Generate query method
		StringBuffer dbQueryDecl = new StringBuffer();

		// Schema "dbQuery" + patternId + idForEvent
		dbQueryDecl.append("'dbQuery_" + patternId.replace("'", "") + "_e" + eventCounter + "'(");
		dbQueryDecl.append(uniqueNameManager
				.getTriplestoreVariable() + ", "); // Mapping
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