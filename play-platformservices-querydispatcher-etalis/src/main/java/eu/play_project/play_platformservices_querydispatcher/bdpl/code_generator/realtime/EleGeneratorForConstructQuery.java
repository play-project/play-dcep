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
	// Returns one triple after the other.
	private Iterator<ElementCep> eventQueryIter;
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
		ele = ElePattern(inQuery);
	}
	
	private String ElePattern(Query inputQuery){
		String elePattern = "";
		
		// Separate operators from patterns.
		EventPatternOperatorCollector eventPatternOperatorCollector =  new EventPatternOperatorCollector();
		eventPatternOperatorCollector.collectValues(inputQuery.getEventQuery());
		binOperatorIter = eventPatternOperatorCollector.getOperators().iterator();
		eventQueryIter = eventPatternOperatorCollector.getEventPatterns().iterator();
		
		// Sub code generators.
		EventPatternEleGenerator eventPatternEleGenerator = new EventPatternEleGenerator();
		ComplexPartEleGenerator complexPartEleGenerator = new ComplexPartEleGenerator();
		VariableTypeManager vtm = new VariableTypeManager(inputQuery);
		
		// Generate complex part.
		elePattern += complexPartEleGenerator.generateCode(inputQuery);

		elePattern += "<-";
		
		// Generate simple part.
		getVarNameManager().processNextEvent();
		
		// Complex event id will be generated if last simple event appeared.
		String complexEventIdCode = "";
		if(!binOperatorIter.hasNext()){
			complexEventIdCode += ",";
			complexEventIdCode += GenerateCEID();
		}
		
		EleEventPattern dbQuery = eventPatternEleGenerator.generateEle(eventQueryIter.next(), inputQuery.getQueryId(), vtm, complexEventIdCode);
		elePattern += dbQuery.getMethodName();
		rdfDbQueries.addAll(dbQuery.getMethodImpl());
		// FIXME sobermeier

		while(binOperatorIter.hasNext()){
			getVarNameManager().processNextEvent();
			String operator = binOperatorIter.next();
			
			if(!binOperatorIter.hasNext()){
				complexEventIdCode += ",";
				complexEventIdCode += GenerateCEID();
			}
			
			dbQuery = eventPatternEleGenerator.generateEle(eventQueryIter.next(), inputQuery.getQueryId(), vtm, complexEventIdCode);
			elePattern += operator;
			elePattern += dbQuery.getMethodName();
			rdfDbQueries.addAll(dbQuery.getMethodImpl());
		}
		
		return elePattern;
	}
	
	private String GenerateCEID(){
		return "random(1000000, 9000000, " + getVarNameManager().getCeid() + ")";
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