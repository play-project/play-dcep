/* Generated By:JJTree: Do not edit this line. ASTPrimitiveValue.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.bdpl.ast;

public
class ASTPrimitiveValue extends SimpleNode {
	private String value;
	
	private String type;
	
	  public void setValue(String s){
		  value = s;
	  }
	  
	  public String getValue(){
		  return value;
	  }
	  
	  public void setType(String s){
		  type = s;
	  }
	  
	  public String getType(){
		  return type;
	  }
	  
	public ASTPrimitiveValue(int id) {
    super(id);
  }

  public ASTPrimitiveValue(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=e808e47ef41d4e7acc66de196bff4e63 (do not edit this line) */