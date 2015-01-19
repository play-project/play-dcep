/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

/**
 * The super class of all global data for a BDPL compiler.
 * 
 * @author ningyuan 
 * 
 * Jan 19, 2015
 *
 */
public class BDPLCompilerData {
	
	protected final String baseURI;

	protected final String bdplQuery;
	
	/*
	 * the compiled BDPL query
	 */
	protected IBDPLQuery compiledQuery;
	
	public BDPLCompilerData(String baseURI, String bdplQuery){
		this.baseURI = baseURI;
		this.bdplQuery = bdplQuery;
	}
	
	public String getBaseURI() {
		return this.baseURI;
	}

	public String getBDPLQuery() {
		return this.bdplQuery;
	}
	
	public void setCompiledQuery(IBDPLQuery compiledQuery){
		this.compiledQuery = compiledQuery;
	}
	
	public IBDPLQuery getCompiledQuery(){
		return compiledQuery;
	}
}
