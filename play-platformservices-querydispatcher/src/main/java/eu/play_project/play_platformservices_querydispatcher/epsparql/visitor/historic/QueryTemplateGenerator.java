package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.historic;

import com.hp.hpl.jena.graph.Node;

import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.QueryTemplate;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.GenerateConstructResulTemplatetVisitor;
import fr.inria.eventcloud.api.Quadruple;

public class QueryTemplateGenerator {
	
	QueryTemplate queryTemplate;
	
	public QueryTemplate createQueryTemplate(){
		
		queryTemplate = new QueryTemplateImpl();

		GenerateConstructResulTemplatetVisitor gtv =  new GenerateConstructResulTemplatetVisitor();
		Node subject, predicate, object;
		
		gtv =  new GenerateConstructResulTemplatetVisitor();
		//triple.getSubject().visitWith(gtv);
		subject = gtv.getTemplate().getSubject();
		
		gtv =  new GenerateConstructResulTemplatetVisitor();
		//triple.getPredicate().visitWith(gtv);
		predicate = gtv.getTemplate().getPredicate();
		
		gtv =  new GenerateConstructResulTemplatetVisitor();
		//triple.getObject().visitWith(gtv);
		object = gtv.getTemplate().getObject();
		
		//queryTemplate.appendLine(new Quadruple(graph, subject, predicate, object));
		
		
		return queryTemplate;
		
	}

}
