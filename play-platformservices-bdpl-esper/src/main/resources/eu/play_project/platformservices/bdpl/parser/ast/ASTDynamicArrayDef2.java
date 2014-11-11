package org.openrdf.query.parser.bdpl.ast;

public
class ASTDynamicArrayDef2 extends SimpleNode implements IArrayDecl{
	private String source;	
	
  public ASTDynamicArrayDef2(int id) {
    super(id);
  }

  public ASTDynamicArrayDef2(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }
  
  public String getSource(){
	  return source;
  }
  
  public void setSource(String s){
	  source = s;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}
