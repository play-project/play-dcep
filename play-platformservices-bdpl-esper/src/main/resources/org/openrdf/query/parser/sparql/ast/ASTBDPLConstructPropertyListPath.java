package org.openrdf.query.parser.bdpl.ast;

public class ASTBDPLConstructPropertyListPath extends SimpleNode {
  public ASTBDPLConstructPropertyListPath(int id) {
    super(id);
  }

  public ASTBDPLConstructPropertyListPath(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
  
	public Node getVerb() {
		return children.get(0);
	}

	public ASTBDPLConstructObjectList getObjectList() {
		return (ASTBDPLConstructObjectList)children.get(1);
	}

	public ASTBDPLConstructPropertyListPath getNextPropertyList() {
		if (children.size() >= 3) {
			return (ASTBDPLConstructPropertyListPath)children.get(2);
		}
		return null;
	}
}
