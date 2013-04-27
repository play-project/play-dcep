package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import static eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.VarNameManager.getVarNameManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;

import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes;
import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes.AgregatedEventType;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.BinOperatorVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventTypeVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.FilterExpressionCodeGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.HavingVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.TriplestoreQueryVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.VarNameManager;
import eu.play_project.querydispatcher.epsparql.Test.helpers.GenerateConstructResultVisitor;


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
	private VarNameManager varNameManager;
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
	
	private String patternId;
	
	//Helper methods.
	private Map<String, AgregatedEventType> variableAgregatedType;
	private QueryTemplate queryTemplate;

	@Override
	public void generateQuery(Query inQuery) {
		//Detect eventtypes
		variableAgregatedType = new AgregatedVariableTypes().detectType(inQuery);

		elePattern = "";
		this.inputQuery = inQuery;
		eventQueryIter = inQuery.getEventQuery().iterator();
		binOperatorIter = inQuery.getEventBinOperator().iterator();
		varNameManager = getVarNameManager();
		varNameManager.newQuery(); // Rest varNameManager.
		varNameManager.setWindowTime(inQuery.getWindow().getValue());
		// Instantiate visitors.
		eventTypeVisitor = new EventTypeVisitor();
		triplestoreQueryVisitor = new TriplestoreQueryVisitor(varNameManager);
		filterExpressionVisitor = new FilterExpressionCodeGenerator();
		binOperatorVisitor =  new BinOperatorVisitor();
		havingVisitor =  new HavingVisitor();
		
		queryTemplate = new QueryTemplateImpl();
		
		// Start code generation.
		ElePattern();
	}
	
	private void ElePattern(){
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
		elePattern += "complex(" + varNameManager.getNextCeid() + "," + patternId + ") do (";
		GenerateConstructResult();
		SaveSharedVariabelValues();
		Having();
		//PrintStatisticsData();
		DecrementReferenceCounter();
		elePattern += ")";
	}

	private void GenerateConstructResult() {
		GenerateConstructResultVisitor generateConstructResultVisitor = new GenerateConstructResultVisitor();
		Iterator<Triple> iter = inputQuery.getConstructTemplate().getTriples()
				.iterator();
		Triple triple;
		while (iter.hasNext()) {
			triple = iter.next();
			if (!containsSharedVariablesTest(triple)) {
				elePattern += "generateConstructResult("
						+ triple.getSubject().visitWith(
								generateConstructResultVisitor)
						+ ","
						+ triple.getPredicate().visitWith(
								generateConstructResultVisitor)
						+ ","
						+ triple.getObject().visitWith(
								generateConstructResultVisitor) + ","
						+ varNameManager.getCeid() + ")";
				if (iter.hasNext()) {
					elePattern += ",";
				}
			}
		}
	}

	private boolean containsSharedVariablesTest(Triple triple){
		boolean result = false;
		if(triple.getSubject().isVariable()){
			if(variableAgregatedType.keySet().contains(triple.getSubject().toString().substring(1))){
				if(variableAgregatedType.get(triple.getSubject().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getSubject().toString().substring(1)).equals(AgregatedEventType.RH)){
					result = true;
				}
			}

		}else if(triple.getPredicate().isVariable()){
			if(variableAgregatedType.keySet().contains(triple.getPredicate().toString().substring(1))){
				if(variableAgregatedType.get(triple.getPredicate().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getPredicate().toString().substring(1)).equals(AgregatedEventType.RH)){
					result = true;
				}
			}
		}else if(triple.getObject().isVariable()){
			if(variableAgregatedType.keySet().contains(triple.getObject().toString().substring(1))){
				if(variableAgregatedType.get(triple.getObject().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getObject().toString().substring(1)).equals(AgregatedEventType.RH)){
					result = true;
				}
			}
		}
		return result;
	}
		
	public void SaveSharedVariabelValues() {
		StringBuffer tmpEle = new StringBuffer();

		Iterator<String> iter = variableAgregatedType.keySet().iterator();

		while (iter.hasNext()) {
			String key = iter.next();

			// Check if variable is in real time and historical part.
			if (variableAgregatedType.get(key).equals(AgregatedEventType.RH) || variableAgregatedType.get(key).equals(AgregatedEventType.CRH)) {
				if (!elePattern.endsWith(",")) {
					elePattern += ",";
				}
				tmpEle.append("variabeValuesAdd(" + patternId + ",'" + key + "'," + "V" + key + ")");
			}

		}

		elePattern += tmpEle.toString();
	}
	
	private void DecrementReferenceCounter(){
		StringBuffer tmp = new StringBuffer();
		
		for (String var : varNameManager.getAllTripleStoreVariablesOfThisQuery()) {
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
		String triplestoreVariable = varNameManager.getNextTriplestoreVariable();
		elePattern += triplestoreVariable;
		elePattern += ") 'WHERE' (";
		AdditionalConditions();
		elePattern += "))";
	}
	
	private void AdditionalConditions(){
		TriplestoreQuery();
		FilterExpression();
		ReferenceCounter();
		//elePattern += ", ";
		//PerformanceMeasurement();
		
		if(!binOperatorIter.hasNext()){
			elePattern += ",";
			GenerateCEID();
		}
	}

	private void ReferenceCounter(){
		elePattern += " incrementReferenceCounter(" + varNameManager.getTriplestoreVariable() + ")";
	}
	
	private void PerformanceMeasurement(){
		elePattern += "measure(" +  patternId + ")";
	}
	
	private void TriplestoreQuery(){
		currentElement.visit(triplestoreQueryVisitor);
		elePattern += triplestoreQueryVisitor.getTriplestoreQueryGraphTerms();
	}

	private void FilterExpression(){
		filterExpressionVisitor.startVisit(((ElementEventGraph)currentElement).getFilterExp());
		if(!elePattern.endsWith(",") && !filterExpressionVisitor.getEle().equals("")){ // This filter is optional. No value needed.
			elePattern += "," + filterExpressionVisitor.getEle();
		}else if(!filterExpressionVisitor.getEle().equals("")){
			elePattern += filterExpressionVisitor.getEle();
		}
	}
		
	private void GenerateCEID(){
		elePattern += "random(1000000, 9000000, " + varNameManager.getCeid() + ")";
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
}