package org.openrdf.query.parser.bdpl.ast;

public 
class ASTArrayVariable extends SimpleNode {
  
  private String name;	
  private String size;
  
  public ASTArrayVariable(int id) {
    super(id);
  }

  public ASTArrayVariable(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }
  
  public void setName(String s){
	  name = s;
  }
  
  public String getName(){
	  return name;
  }
  
  public void setSize(String s){
	  size = s;
  }
  
  public String getSize(){
	  return size;
  }
  
  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}