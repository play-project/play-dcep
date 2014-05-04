package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementBraceOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;


/**
 * Generates "TriplestoreQuery" described in BDPL grammar.
 * @author sobermeier
 *
 */
public class TriplestoreQueryVisitor extends GenericVisitor {
	
	private String triplestoreQuery;
	private final UniqueNameManager uniqueNameManager;
	private String aggregateValuesCode;
	VariableTypeManager vtm;

	public TriplestoreQueryVisitor(UniqueNameManager uniqueNameManager, VariableTypeManager vtm){
		triplestoreQuery = "";
		aggregateValuesCode = "";
		this.uniqueNameManager = uniqueNameManager;
		this.vtm = vtm;
	}
	
	@Override
	public void visit(ElementEventGraph el) {
		// Visit triples
		triplestoreQuery = "";
		aggregateValuesCode = "";
		el.getElement().visit(this);
	}
	
	@Override
	public void visit(ElementGroup el) {
		// Visit all group elements
		for(int i=0; i<el.getElements().size(); i++){
			el.getElements().get(i).visit(this);
		}
	}
	
	
	public String getTriplestoreQueryGraphTerms() {
		return triplestoreQuery;
	}

	@Override
	public Object visitAny(Node_ANY it) {
		return null;
	}

	@Override
	public Object visitBlank(Node_Blank it, AnonId id) {
		throw new RuntimeException("Node_Blank is not allowed.");
	}

	@Override
	public Object visitLiteral(Node_Literal it, LiteralLabel lit) {
		triplestoreQuery += quoteForProlog(lit.getLexicalForm()) + ", ";
		return lit;
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		triplestoreQuery += quoteForProlog(uri) + ", ";
		return uri;
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
		
		//Add code to save values.
		if(vtm.isType(name, VariableTypes.SIMPLE_TYPE)){
			logger.error("VariableTypes.SAMPLE_TYPE is not implemented in dETALIS");
		}else if(vtm.isType(name, VariableTypes.COUNT_TYPE)){
			logger.error("VariableTypes.COUNT_TYPE is not implemented in dETALIS");
		}else if(vtm.isType(name, VariableTypes.AVG_TYPE)){
			aggregateValuesCode += ", addAgregatValue(" + uniqueNameManager.getAggrDbId() + ", " + "V" + name + ")";
		}else if(vtm.isType(name, VariableTypes.MIN_TYPE)){
			aggregateValuesCode += ", storeMin(" + uniqueNameManager.getAggrDbId() + ", " + "V" + name + ")";
		}else if(vtm.isType(name, VariableTypes.MIN_TYPE)){
			aggregateValuesCode += ", storeMin(" + uniqueNameManager.getAggrDbId() + ", " + "V" + name + ")";
		}else if(vtm.isType(name, VariableTypes.SUM_TYPE)){
			aggregateValuesCode += ", sumAdd(" + uniqueNameManager.getAggrDbId() + ", " + "V" + name + ")";
		}
		
		
		StringBuffer resultNode = new StringBuffer();
		//It is part of a blank node.
		if (name.startsWith("?")) {
			// Transform number to uppercase char.
			for (int i = 1; i < name.length(); ++i) {
				char c = name.charAt(i);
				resultNode.append("V" +(char)(c+17));
				resultNode.append(", ");
			}
			triplestoreQuery += resultNode.toString();
			return resultNode.toString();
		}else{
			//It is a ordinary vraible.
			triplestoreQuery += "V" + name + ", ";
			return "V" + name;
		}
	}


	@Override
	public void visit(ElementPathBlock el) {
		Iterator<TriplePath> iter = el.getPattern().getList().iterator();
		
		// Generate db queries.
		while (iter.hasNext()) {
			
			triplestoreQuery += "rdf(";
			TriplePath tmpTriplePath = iter.next();
			// Get data from one graph
			tmpTriplePath.getSubject().visitWith(this);
			tmpTriplePath.getPredicate().visitWith(this);
			tmpTriplePath.getObject().visitWith(this);
			triplestoreQuery += uniqueNameManager.getTriplestoreVariable() + ")";
			
			//Add save aggregate values code if it exists.
			if(!aggregateValuesCode.equals("")){
				triplestoreQuery += aggregateValuesCode;
			}
			
			if(iter.hasNext()){
				triplestoreQuery += ", ";
			}
		}
	}
	
	@Override
	public void visit(ElementEventBinOperator el) {
		el.getLeft().visit(this);
		triplestoreQuery = "'" + el.getTyp() + "'";
		el.getRight().visit(this);
	}
	
	@Override
	public void visit(ElementBraceOperator el) {
		el.getSubElements().visit(this);
	}
}
