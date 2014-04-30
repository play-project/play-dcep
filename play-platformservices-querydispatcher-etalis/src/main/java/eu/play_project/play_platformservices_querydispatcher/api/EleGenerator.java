package eu.play_project.play_platformservices_querydispatcher.api;

import java.util.List;

import com.hp.hpl.jena.query.Query;

import eu.play_project.play_platformservices.api.QueryTemplate;

public interface EleGenerator {
	public void generateQuery(Query inQuery); 
	public void setPatternId(String patternId);
	public String getEle();
	public QueryTemplate getQueryTemplate();
	public List<String[]> getEventProperties();
	public List<String> getRdfDbQueries();	
}