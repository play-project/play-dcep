/* Generated By:JJTree: Do not edit this line. ASTIRIFunc.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.sparql.ast;

public class ASTIRIFunc extends SimpleNode {

	private String baseURI;

	public ASTIRIFunc(int id) {
		super(id);
	}

	public ASTIRIFunc(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	/**
	 * @param baseURI
	 *        The baseURI to set.
	 */
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * @return Returns the baseURI.
	 */
	public String getBaseURI() {
		return baseURI;
	}
}
/* JavaCC - OriginalChecksum=24686ad25db647184bbe120af4ee3c5c (do not edit this line) */
