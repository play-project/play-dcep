package eu.play_project.play_platformservices.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.eventcloud.api.Quadruplable;

/**
 * Represents informations extracted from a EP-SPARQL 2.0 query.
 * With this informations no additional parsing is required to deal with a query in this system.
 * 
 * @author sobermeier
 *
 */
public class QueryDetails implements Serializable {
	private static final long serialVersionUID = -8156425318534996557L;
	private String queryId;
	private List<String> inputStreams;
	private String outputStream;
	private String windowTime = "";
	
	public QueryDetails(){}
	

	public QueryDetails(String queryId){
		this.queryId = queryId;
	}
	
	public String getQueryId() {
		return queryId;
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
		return inputStreams;
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
		return outputStream;
	}
	
	public void setOutputStream(String outputStream) {
		this.outputStream = outputStream;
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
