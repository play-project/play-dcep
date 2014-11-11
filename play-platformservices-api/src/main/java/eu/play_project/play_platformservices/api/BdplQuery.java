package eu.play_project.play_platformservices.api;

import java.io.Serializable;
import java.util.List;

public class BdplQuery implements Serializable {

	private static final long serialVersionUID = 100L;

	private final QueryDetails queryDetails;
	private final String bdplQuery;
	private final String targetQuery;
	private final QueryTemplate constructTemplate;
	private final List<HistoricalQuery> historicalQueries;
	
	
	public BdplQuery(Builder builder) {
		this.queryDetails = builder.queryDetails;
		this.bdplQuery = builder.bdplQuery;
		this.targetQuery = builder.targetQuery;
		this.constructTemplate = builder.constructTemplate;
		this.historicalQueries = builder.historicalQueries;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * This builder can be made to build a {@linkplain BdplQuery} <b>without</b>
	 * checking consistency. This means that there is no checking whether all
	 * setters were invoked (with not {@code null} values) before building. Useful e.g.
	 * in UnitTests where inconsistent {@linkplain BdplQuery} are acceptable.
	 */
	public static Builder nonValidatingBuilder() {
		return new Builder(false);
	}

	public QueryDetails getDetails() {
		return queryDetails;
	}
	
	/**
	 * The BDPL query.
	 */
	public String getBdpl() {
		return bdplQuery;
	}
	
	/**
	 * The target query generated from BDPL for the underlying event processing engine.
	 */
	public String getTargetQuery() {
		return targetQuery;
	}

	/**
	 * The CONSTRUCT template to be instantiated with results from the query.
	 */
	public QueryTemplate getConstructTemplate() {
		if(constructTemplate == null){
			throw new RuntimeException("No construct template found.");
		}else{
			return constructTemplate;
		}
	}
	
	/**
	 * The historical queries to be invoked once the real-time query is fulfilled.
	 */
	public List<HistoricalQuery> getHistoricalQueries() {
		return historicalQueries;
	}
	
	@Override
	public String toString() {
		return getBdpl();
	}
	
	public static class Builder {

		private boolean validating;

		public Builder() {
			new Builder(true);
		}
		
		public Builder (boolean validating) {
			this.validating = validating;
		}
		
		private QueryDetails queryDetails;
		private String bdplQuery;
		private String targetQuery;
		private QueryTemplate constructTemplate;
		private List<HistoricalQuery> historicalQueries;

		public Builder details(QueryDetails queryDetails) {
			this.queryDetails = queryDetails;
			return this;
		}

		public Builder bdpl(String bdplQuery) {
			this.bdplQuery = bdplQuery;
			return this;
		}
		
		public Builder historicalQueries(List<HistoricalQuery> historicalQueries) {
			this.historicalQueries = historicalQueries;
			return this;
		}
		
		public Builder target(String targetQuery) {
			this.targetQuery = targetQuery;
			return this;
		}

		/**
		 * Set template for historical queries with placeholder for shared variables.
		 */
		public Builder constructTemplate(QueryTemplate constructTemplate) {
			this.constructTemplate = constructTemplate;
			return this;
		}
		
		public BdplQuery build() {
			if (validating) {
				validate();
			}
			return new BdplQuery(this);
		}
		
		private void validate() {
			if (queryDetails == null) {
				throw new IllegalStateException("queryDetails was not set on builder.");
			}
			if (bdplQuery == null) {
				throw new IllegalStateException("bdplQuery was not set on builder.");
			}
			if (targetQuery == null) {
				throw new IllegalStateException("targetQuery was not set on builder.");
			}
			if (constructTemplate == null) {
				throw new IllegalStateException("constructTemplate was not set on builder.");
			}
			if (historicalQueries == null) {
				throw new IllegalStateException("historicalQueries was not set on builder.");
			}
		}
	}
}
