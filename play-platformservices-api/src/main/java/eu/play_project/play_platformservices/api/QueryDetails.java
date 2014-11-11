package eu.play_project.play_platformservices.api;

import java.io.Serializable;
import java.util.HashSet;
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
	
	public QueryDetails(){} // JAXB needs this
	
	/**
	 * @deprecated Use the builder instead from {@link #builder()}.
	 */
	@Deprecated
	public QueryDetails(String queryId) {
		this.queryId = queryId;
	}
	
	public QueryDetails(Builder builder) {
		this.queryId = builder.queryId;
		this.inputStreams = builder.inputStreams;
		this.outputStream = builder.outputStream;
		this.historicStreams = builder.historicStreams;
	}
	
	public static Builder builder() {
		return new Builder();
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
	
	public static class Builder {
		private String queryId;
		private Set<String> inputStreams;
		private String outputStream;
		private Set<String> historicStreams;
		
		public Builder() {
		}
		
		public Builder id(String queryId) {
			this.queryId = queryId;
			return this;
		}
		
		public Builder inputStreams(Set<String> inputStreams) {
			this.inputStreams = inputStreams;
			return this;
		}

		public Builder outputStream(String outputStream) {
			this.outputStream = outputStream;
			return this;
		}

		public Builder historicStreams(Set<String> historicStreams) {
			this.historicStreams = historicStreams;
			return this;
		}
		
		public QueryDetails build() {
			validate();
			return new QueryDetails(this);
		}
		
		private void validate() {
			if (queryId == null) {
				throw new IllegalStateException("queryId was not set on builder.");
			}
			if (inputStreams == null) {
				throw new IllegalStateException("inputStreams was not set on builder.");
			}
			if (outputStream == null) {
				throw new IllegalStateException("outputStream was not set on builder.");
			}
			if (historicStreams == null) {
				historicStreams = new HashSet<String>();
			}
		}

	}
}
