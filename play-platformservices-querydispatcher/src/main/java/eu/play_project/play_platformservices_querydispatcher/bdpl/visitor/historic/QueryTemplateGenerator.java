package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenerateConstructResulTemplatetVisitor;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;
import fr.inria.eventcloud.api.Quadruple;

public class QueryTemplateGenerator {

	QueryTemplate queryTemplate;
	
	/**
	 * Add all triples with variables shared between construct and historic part.
	 * @param inputQuery Parsed jena query.
	 * @return QueryTemplate with triples.
	 */
	public QueryTemplate createQueryTemplate(Query inputQuery) {
		Node graph = NodeFactory.createURI("urn:placeholder");
		queryTemplate = new QueryTemplateImpl();
		
		VariableTypeManager variableTypeManager = new VariableTypeManager(inputQuery);
		variableTypeManager.collectVars();
		
		boolean tripleContainsHistoricalAndContrtructVars =  false;
		for (Triple triple : inputQuery.getConstructTemplate().getTriples()) {
			if (triple.getSubject().isVariable()) {
				tripleContainsHistoricalAndContrtructVars = variableTypeManager.isType(triple.getSubject().getName(), VariableTypes.HISTORIC_TYPE) && variableTypeManager.isType(triple.getSubject().getName(), VariableTypes.CONSTRUCT_TYPE);
			} else if (triple.getPredicate().isVariable()) {
				tripleContainsHistoricalAndContrtructVars = variableTypeManager.isType(triple.getPredicate().getName(), VariableTypes.HISTORIC_TYPE) && variableTypeManager.isType(triple.getPredicate().getName(), VariableTypes.CONSTRUCT_TYPE);
			} else if (triple.getObject().isVariable()) {
				tripleContainsHistoricalAndContrtructVars = variableTypeManager.isType(triple.getObject().getName(), VariableTypes.HISTORIC_TYPE) && variableTypeManager.isType(triple.getObject().getName(), VariableTypes.CONSTRUCT_TYPE);
			}
			
			if(tripleContainsHistoricalAndContrtructVars){
				queryTemplate.appendLine(new Quadruple(graph, triple.getSubject(), triple.getPredicate(), triple.getObject()));
			}
		}

		return queryTemplate;
	}
}
