package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.historic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;

import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.GenerateConstructResulTemplatetVisitor;
import fr.inria.eventcloud.api.Quadruple;

public class QueryTemplateGenerator {

	QueryTemplate queryTemplate;
	
	public QueryTemplate createQueryTemplate(Query inputQuery) {
		Node graph = Node.createURI("urn:placeholder");
		queryTemplate = new QueryTemplateImpl();

		Iterator<Triple> iter = inputQuery.getConstructTemplate().getTriples().iterator();

		Triple triple;
		while (iter.hasNext()) {
			triple = iter.next();
			GenerateConstructResulTemplatetVisitor gtv = new GenerateConstructResulTemplatetVisitor();
			Node subject, predicate, object;

			gtv = new GenerateConstructResulTemplatetVisitor();
			triple.getSubject().visitWith(gtv);
			subject = gtv.getTemplate().getSubject();

			gtv = new GenerateConstructResulTemplatetVisitor();
			triple.getPredicate().visitWith(gtv);
			predicate = gtv.getTemplate().getPredicate();

			gtv = new GenerateConstructResulTemplatetVisitor();
			triple.getObject().visitWith(gtv);
			object = gtv.getTemplate().getObject();

			queryTemplate.appendLine(new Quadruple(graph, subject, predicate, object));

		}

		return queryTemplate;

	}
	
	private boolean containsSharedVariables(Query query){
		
		Map<String, Integer> vars = new HashMap<String, Integer>();
		
		query.getConstructTemplate();
		
		return false;
	}

}
