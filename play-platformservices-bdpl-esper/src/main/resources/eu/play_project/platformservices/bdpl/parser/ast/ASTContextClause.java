package org.openrdf.query.parser.bdpl.ast;

public
class ASTContextClause extends SimpleNode {
	private boolean arrayDef = false;
	
	
	public void setStaticArrayDecl(boolean d){
	    arrayDef = d;
	}
	  
	public boolean isStaticArrayDecl(){
		return arrayDef;
	}
	
  public ASTContextClause(int id) {
    super(id);
  }

  public ASTContextClause(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}
