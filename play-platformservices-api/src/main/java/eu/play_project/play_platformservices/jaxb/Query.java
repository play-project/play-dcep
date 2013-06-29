package eu.play_project.play_platformservices.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

import eu.play_project.play_platformservices.api.CepQuery;

/**
 * A simple bean to encapsulate parts of a {@linkplain CepQuery} for WS marshalling.
 * 
 * @author Christophe Hamerling
 * @author Roland Stühmer
 */
@XmlRootElement
public class Query {
	
	public Query() {};
	
	public Query(CepQuery cepQuery) {
		this.id = cepQuery.getQueryDetails().getQueryId();
		this.content = cepQuery.getEpSparqlQuery();
	};
	

    public String name;

    public String id;

    public String content;

    public String recordDate;
}