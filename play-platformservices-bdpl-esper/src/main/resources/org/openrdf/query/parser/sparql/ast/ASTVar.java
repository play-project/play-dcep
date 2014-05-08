/* Generated By:JJTree: Do not edit this line. ASTVar.java */

package org.openrdf.query.parser.bdpl.ast;

public class ASTVar extends SimpleNode {

	private String name;

	private boolean anonymous;

	public ASTVar(int id) {
		super(id);
	}

	public ASTVar(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	@Override
	public String toString()
	{
		return super.toString() + " (" + name + ")";
	}
}
