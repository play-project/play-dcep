package eu.play_project.play_platformservices.api;

import java.util.List;

/**
 * Represents informations extracted from a BDPL query.
 * With this informations no additional parsing is required to deal with a query in this system.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 *
 */
public class QueryDetailsEtalis extends QueryDetails {
	private static final long serialVersionUID = 100L;
	private String etalisProperty;
	private String tumblingWindow;
	private List<String> rdfDbQueries;
	private String complexType;  //Type name of the complex event.
	
	public QueryDetailsEtalis(){} // JAXB needs this
	
	public QueryDetailsEtalis(String queryId){
		super(queryId);
		// Init with valid values which have no functional effect.
		this.etalisProperty = "";
		this.tumblingWindow = "true";
	}

	public String getEtalisProperty() {
		return etalisProperty;
	}

	public void setEtalisProperty(String etalisProperty) {
		this.etalisProperty = etalisProperty;
	}

	public String getTumblingWindow() {
		return tumblingWindow;
	}

	public void setTumblingWindow(String tumblingWindow) {
		this.tumblingWindow = tumblingWindow;
	}

	public List<String> getRdfDbQueries() {
		return rdfDbQueries;
	}

	public void setRdfDbQueries(List<String> rdfDbQueries) {
		this.rdfDbQueries = rdfDbQueries;
	}

	public String getComplexType() {
		return complexType;
	}

	public void setComplexType(String complexType) {
		this.complexType = complexType;
	}
	
	// TODO stuehmer: add a builder for QueryDetails to validate a few mandatory settings upon build()
}
