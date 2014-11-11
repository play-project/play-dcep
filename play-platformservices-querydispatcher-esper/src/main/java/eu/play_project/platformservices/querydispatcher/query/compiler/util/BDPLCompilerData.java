/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.util;


import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLVarTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.ConstructTemplate;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.realtime.util.EPLTranslationData;

/**
 * @author ningyuan 
 * 
 * Aug 25, 2014
 *
 */
public class BDPLCompilerData {
	
	private final String baseURI;

	private final String bdplQuery;
	
	private ASTQueryContainer queryContainer;
	
	private String prologText;
	
	private BDPLVarTable varTable;
	
	private BDPLArrayTable arrayTable;
	
	private ConstructTemplate constructTemplate;
	
	private EPLTranslationData eplTranslationData;
	
	private SubQueryTable subQueryTable;
	
	private IBDPLQuery compiledQuery;
	
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
	
	public ASTQueryContainer getQueryContainer() {
		return this.queryContainer;
	}

	public void setQueryContainer(ASTQueryContainer queryContainer) {
		this.queryContainer = queryContainer;
	}

	
	public void setPrologText(String prologText) {
		this.prologText = prologText;
	}
	
	public String getPrologText() {
		return this.prologText;
	}
	
	public BDPLVarTable getVarTable() {
		return this.varTable;
	}

	public void setVarTable(BDPLVarTable varTable) {
		this.varTable = varTable;
	}

	public BDPLArrayTable getArrayTable() {
		return this.arrayTable;
	}

	public void setArrayTable(BDPLArrayTable arrayTable) {
		this.arrayTable = arrayTable;
	}
	
	public ConstructTemplate getConstructTemplate() {
		return this.constructTemplate;
	}

	public void setConstructTemplate(ConstructTemplate constructTemplate) {
		this.constructTemplate = constructTemplate;
	}
	
	public EPLTranslationData getEPLTranslationData() {
		return this.eplTranslationData;
	}

	public void setEPLTranslationData(EPLTranslationData eplTranslationData) {
		this.eplTranslationData = eplTranslationData;
	}
	
	public SubQueryTable getSubQueryTable() {
		return this.subQueryTable;
	}

	public void setSubQueryTable(SubQueryTable subQueryTable) {
		this.subQueryTable = subQueryTable;
	}
	
	public IBDPLQuery getCompiledQuery() {
		return this.compiledQuery;
	}

	public void setCompiledQuery(IBDPLQuery compiledQuery) {
		this.compiledQuery = compiledQuery;
	}
}
