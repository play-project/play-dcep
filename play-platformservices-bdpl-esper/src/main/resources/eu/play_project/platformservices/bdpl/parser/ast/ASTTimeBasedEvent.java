/* Generated By:JJTree: Do not edit this line. ASTTimeBasedEvent.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.bdpl.ast;

public
class ASTTimeBasedEvent extends SimpleNode {
	private String operator;
	
	public void setOperator(String op){
	    operator = op;
	}
	  
	public String getOperator(){
		return operator;
	}
	
	
  public ASTTimeBasedEvent(int id) {
    super(id);
  }

  public ASTTimeBasedEvent(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}
