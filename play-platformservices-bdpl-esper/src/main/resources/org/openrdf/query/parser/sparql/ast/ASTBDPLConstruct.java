package org.openrdf.query.parser.bdpl.ast;

public class ASTBDPLConstruct extends SimpleNode{
	
	public ASTBDPLConstruct(int id) {
		super(id);
	}

	public ASTBDPLConstruct(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean isWildcard() {
		return this.jjtGetNumChildren() == 0;
	}
}
