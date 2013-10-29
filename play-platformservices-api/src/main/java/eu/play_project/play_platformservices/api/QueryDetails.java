package eu.play_project.play_platformservices.api;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents informations extracted from a BDPL query.
 * With this informations no additional parsing is required to deal with a query in this system.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 *
 */
@XmlRootElement
public class QueryDetails implements Serializable {
	private static final long serialVersionUID = 100L;
	private String queryId;
	private Set<String> inputStreams;
	private String outputStream;
	private Set<String> historicStreams;
	private String etalisProperty;
	private String tumblingWindow;
	private List<String> rdfDbQueries;
	private String complexType;  //Type name of the complex event.
	
	public QueryDetails(){} // JAXB needs this
	
	public QueryDetails(String queryId){
		this.queryId = queryId;
		// Init with valid values which have no functional effect.
		this.etalisProperty = "";
		this.tumblingWindow = "true";
	}
	
	public String getQueryId() {
		return this.queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	
	/**
	 * Provides the set of input event streams in a query. This representation
	 * omits the trailing {@code #stream} suffix so the stream IDs can be used
	 * with DSB and EC.
	 */
	public Set<String> getInputStreams() {
		return this.inputStreams;
	}
	
	public void setInputStreams(Set<String> set) {
		this.inputStreams = set;
	}
	
	/**
	 * Provides the output event streams in a query. This representation
	 * omits the trailing {@code #stream} suffix so the stream ID can be used
	 * with DSB and EC.
	 */
	public String getOutputStream() {
		return this.outputStream;
	}
	
	public void setOutputStream(String outputStream) {
		this.outputStream = outputStream;
	}

	/**
	 * Provides the historic event streams in a query. This representation
	 * omits the trailing {@code #stream} suffix so the stream ID can be used
	 * with DSB and EC.
	 */
	public Set<String> getHistoricStreams() {
		return this.historicStreams;
	}
	
	public void setHistoricStreams(Set<String> historicStreams) {
		this.historicStreams = historicStreams;
	}
	
	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("queryId=").append(queryId).append(", ");
        sb.append("inputStreams=").append(inputStreams).append(", ");
        sb.append("outputStream=").append(outputStream).append(", ");
        sb.append("historicStreams=").append(historicStreams).append(", ");
        sb.append("... ");
        sb.append("}");
		return sb.toString();
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
