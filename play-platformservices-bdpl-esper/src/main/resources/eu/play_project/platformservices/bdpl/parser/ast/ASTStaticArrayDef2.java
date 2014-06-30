package org.openrdf.query.parser.bdpl.ast;

public
class ASTStaticArrayDef2 extends SimpleNode implements ArrayDef{
	private String source;	
	
  public ASTStaticArrayDef2(int id) {
    super(id);
  }

  public ASTStaticArrayDef2(SyntaxTreeBuilder p, int id) {
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