package eu.play_project.play_platformservices.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

import eu.play_project.play_platformservices.api.EpSparqlQuery;

/**
 * A simple bean to encapsulate parts of a {@linkplain EpSparqlQuery} for WS marshalling.
 * 
 * @author Christophe Hamerling
 * @author Roland Stühmer
 */
@XmlRootElement
public class Query {

    public String name;

    public String id;

    public String content;

    public String recordDate;
}