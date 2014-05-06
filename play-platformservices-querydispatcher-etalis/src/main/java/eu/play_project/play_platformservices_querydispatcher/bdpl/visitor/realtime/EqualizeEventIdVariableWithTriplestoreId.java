package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;

/**
 * In some queries the event id is a variable. Event data are identified by the event id, in the triple store.
 * To fill the id variable of the query it is necessary to map the triple store id to this variable.
 * 
 * @author sobermeier
 *
 */
public class EqualizeEventIdVariableWithTriplestoreId extends GenericVisitor {
	
	private String equalizeCode;
	private UniqueNameManager uniqueNameManager;

	public EqualizeEventIdVariableWithTriplestoreId(UniqueNameManager uniqueNameManager) {
		this.uniqueNameManager = uniqueNameManager;
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		if (el.getGraphNameNode().isVariable()) {
			equalizeCode = el.getGraphNameNode().visitWith(this).toString();
			equalizeCode += "=" + uniqueNameManager.getTriplestoreVariable();
			equalizeCode += ", ";
		} else {
			equalizeCode = "";
		}
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		return equalizeCode = " V" + name;
	}

	@Override
	public void visit(ElementNotOperator el) {
		el.getStart().visit(this);
		el.getEnd().visit(this);
		el.getNot().visit(this);
	}
	
	public String getEqualizeCode() {
		return this.equalizeCode;
	}

}
