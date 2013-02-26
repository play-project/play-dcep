package eu.play_project.play_platformservices.api;

import java.io.Serializable;
import java.util.List;

/**
 * Represents informations extracted from a EP-SPARQL 2.0 query.
 * With this informations no additional parsing is required to deal with a query in this system.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 *
 */
public class QueryDetails implements Serializable {
	private static final long serialVersionUID = -8156425318534996557L;
	private String queryId;
	private List<String> inputStreams;
	private String outputStream;
	private List<String> historicStreams;
	private String windowTime = "";
	
	public QueryDetails(){}
	
	public QueryDetails(String queryId){
		this.queryId = queryId;
	}
	
	public String getQueryId() {
		return this.queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	
	/**
	 * Provides the List of input event streams in a query. This representation
	 * omits the trailing {@code #stream} suffix so the stream IDs can be used
	 * with DSB and EC.
	 */
	public List<String> getInputStreams() {
		return this.inputStreams;
	}
	
	public void setInputStreams(List<String> inputStreams) {
		this.inputStreams = inputStreams;
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
	public List<String> getHistoricStreams() {
		return this.historicStreams;
	}
	
	public void setHistoricStreams(List<String> historicStreams) {
		this.historicStreams = historicStreams;
	}
	
	/**
	 * Set the window length in seconds.
	 */
	public void setWindowTime(String windowTime) {
		this.windowTime = windowTime;
	}

	/**
	 * Get the window length in seconds.
	 */
	public String getWindowTime() {
		return windowTime;
	}
}
