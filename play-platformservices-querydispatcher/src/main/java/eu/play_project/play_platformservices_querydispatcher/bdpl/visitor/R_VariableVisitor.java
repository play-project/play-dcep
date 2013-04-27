//package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import com.hp.hpl.jena.graph.Node;
//import com.hp.hpl.jena.graph.Node_Variable;
//import com.hp.hpl.jena.sparql.core.TriplePath;
//import com.hp.hpl.jena.sparql.syntax.Element;
//import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
//import com.hp.hpl.jena.sparql.syntax.ElementGroup;
//import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
//
//import eu.play_platform.platformservices.bdpl.VariableTypes;
//import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.GenericVisitor;
//import eu.play_project.play_platformservices_querydispatcher.types.C_Quadruple;
//import eu.play_project.play_platformservices_querydispatcher.types.H_Quadruple;
//import eu.play_project.play_platformservices_querydispatcher.types.P_Quadruple;
//import eu.play_project.play_platformservices_querydispatcher.types.R_Quadruple;
//import fr.inria.eventcloud.api.Quadruple;
//
//public class R_VariableVisitor extends GenericVisitor {
//	Map<String, List<Quadruple>> variables;
//
//	public Map<String, List<Quadruple>> getVariables() {
//		return variables;
//	}
//
//	public void setVariables(Map<String, List<Quadruple>> variables) {
//		this.variables = variables;
//	}
//
//	@Override
//	public void visit(ElementEventGraph el) {
//		el.getElement().visit(this);
//	}
//
//	@Override
//	public void visit(ElementGroup el) {
//		for (Element elment : el.getElements()) {
//			elment.visit(this);
//		}
//	}
//
//	@Override
//	public void visit(ElementPathBlock el) {
//		for (TriplePath triple : el.getPattern()) {
//			addToVariablelist(triple.getSubject().visitWith(this), triple,
//					VariableTypes.realtimeType);
//			addToVariablelist(triple.getPredicate().visitWith(this), triple,
//					VariableTypes.realtimeType);
//			addToVariablelist(triple.getObject().visitWith(this), triple,
//					VariableTypes.realtimeType);
//		}
//	}
//
//	@Override
//	public Object visitVariable(Node_Variable it, String name) {
//		return name;
//	}
//
//	// Add value to resultSet if it is not null.
//	private void addToVariablelist(Object var, TriplePath triple,
//			VariableTypes type) {
//		if (variables == null) {
//			throw new RuntimeException(
//					"Pleas use first R_VariableVisitor.setVariables(Map<String, List<Quadruple>> variables) first, before using visit(R_VariableVisitor rVisitor)");
//		}
//		if (var != null) {
//			if (variables.get(var) == null) {
//				variables.put((String) var, new ArrayList<Quadruple>());
//			}
//			List<Quadruple> value = variables.get(var);
//			switch (type) {
//			case constructType:
//				value.add(new C_Quadruple(Node
//						.createURI("http://construct.play-project.eu/"), triple
//						.getSubject(), triple.getPredicate(), triple
//						.getObject()));
//				break;
//			case historicType:
//				value.add(new H_Quadruple(Node
//						.createURI("http://construct.play-project.eu/"), triple
//						.getSubject(), triple.getPredicate(), triple
//						.getObject()));
//				break;
//			case realtimeType:
//				value.add(new R_Quadruple(Node
//						.createURI("http://construct.play-project.eu/"), triple
//						.getSubject(), triple.getPredicate(), triple
//						.getObject()));
//				break;
//			case preloadType:
//				value.add(new P_Quadruple(Node
//						.createURI("http://construct.play-project.eu/"), triple
//						.getSubject(), triple.getPredicate(), triple
//						.getObject()));
//			}
//
//			variables.put((String) var, value);
//		}
//	}
//}
