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
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexEventEleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.CountEventsVisitor;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EqualizeEventIdVariableWithTriplestoreId;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventMembersFromStream;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.EventPatternEleGenerator;
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
	private Element currentElement;
	private Iterator<String> binOperatorIter;
	private LinkedList<String> rdfDbQueries;
	QueryTemplate queryTemplate;
	private String ele;

	@Override
	public void generateQuery(Query inQuery) {
		UniqueNameManager uniqueNameManager = getVarNameManager();
		
		uniqueNameManager.setWindowTime(inQuery.getWindow().getValue());
		
		// Instantiate visitors.
		CountEventsVisitor eventCounter =  new CountEventsVisitor();
		
		// Collect basic informations like number of events or variable types.
		uniqueNameManager.newQuery(eventCounter.count(inQuery.getEventQuery()));
		
		this.queryTemplate = new QueryTemplateImpl();
		this.rdfDbQueries = new LinkedList<String>();
		
		
		// Start code generation.
		//ElePattern(elePattern, inputQuery);
	}
	
	private void ElePattern(String elePattern, Query inputQuery){
		
		// Separate operators from patterns.
		EventPatternOperatorCollector eventPatternOperatorCollector =  new EventPatternOperatorCollector();
		eventPatternOperatorCollector.collectValues(inputQuery.getEventQuery());
		binOperatorIter = eventPatternOperatorCollector.getOperators().iterator();
		eventQueryIter = eventPatternOperatorCollector.getEventPatterns().iterator();
		
		EventPatternEleGenerator eventPatternEleGenerator = new EventPatternEleGenerator();
		
		Complex(elePattern, patternId, inputQuery);
		elePattern += "<-";
		currentElement = eventQueryIter.next();
		SimpleEventPattern(elePattern, currentElement);

		while(binOperatorIter.hasNext()){
			elePattern += binOperatorIter.next();
			currentElement = eventQueryIter.next();
			SimpleEventPattern(elePattern, currentElement);
		}
	}
	
	//Call prolog methods which echos statistics data to the console.
	private void PrintStatisticsData(String elePattern){
		elePattern += ", printRdfStat, printRefCountN";
	}
	
//	public void SimpleEventPattern(String elePattern, Element element) {
//		EventTypeVisitor eventTypeVisitor = new EventTypeVisitor();
//		UniqueNameManager uniqueNameManager = getVarNameManager();
//	
//		elePattern += "(";
//		element.visit(eventTypeVisitor);
//		elePattern += eventTypeVisitor.getEventType();
//		elePattern += "(";
//		elePattern += uniqueNameManager.getTriplestoreVariable();
//		elePattern += ") 'WHERE' (";
//		AdditionalConditions(elePattern, binOperatorIter);
//		elePattern += "))";
//	}
	
//	private void AdditionalConditions(String elePattern, Iterator<String> binOperatorIter){
//		LinkedList<String> rdfDbQueries = TriplestoreQuery(elePattern, currentElement);
//		EventIdVarIsSynonymousWithTriplestoreId(elePattern, currentElement);
//		ReferenceCounter(elePattern);
////		elePattern += ", ";
////		PerformanceMeasurement();
//		
//		if(!binOperatorIter.hasNext()){
//			elePattern += ",";
//			GenerateCEID(elePattern);
//		}
//	}

	private void PerformanceMeasurement(String elePattern){
		elePattern += "measure(" +  patternId + ")";
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
		return ele;
	}
}