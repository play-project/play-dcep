package eu.play_project.play_platformservices.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

import eu.play_project.play_platformservices.api.BdplQuery;

/**
 * A simple bean to encapsulate parts of a {@linkplain BdplQuery} for WS marshalling.
 * 
 * @author Christophe Hamerling
 * @author Roland Stühmer
 */
@XmlRootElement
public class Query {
	
	public Query() {};
	
	public Query(BdplQuery bdplQuery) {
		this.id = bdplQuery.getQueryDetails().getQueryId();
		this.content = bdplQuery.getEpSparqlQuery();
	};
	

    public String name;

    public String id;

    public String content;

    public String recordDate;
}