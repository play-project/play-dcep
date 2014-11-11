package eu.play_project.play_platformservices_querydispatcher.api;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;

import eu.play_project.play_platformservices.api.QueryTemplate;
import fr.inria.eventcloud.api.Quadruple;

public interface EleGenerator {
	public void generateQuery(Query inQuery);
	public void setPatternId(String patternId);
	public String getEle();
	public QueryTemplate<Quadruple, Quadruple, Node> getQueryTemplate();
	public List<String[]> getEventProperties();
	public List<String> getRdfDbQueries();
}
