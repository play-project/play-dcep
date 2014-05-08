
package org.openrdf.query.parser.bdpl.ast;

public class ASTWindowDecl extends SimpleNode{
	
	private String type;
	
	public void setType(String t){
		type = t;
	}
	
	public String getType(){
		return type;
	}
	
	public ASTWindowDecl(int id) {
		super(id);
	}

	public ASTWindowDecl(SyntaxTreeBuilder p, int id) {
	    super(p, id);
	}


	/** Accept the visitor. **/
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
	  return visitor.visit(this, data);
	}
}
