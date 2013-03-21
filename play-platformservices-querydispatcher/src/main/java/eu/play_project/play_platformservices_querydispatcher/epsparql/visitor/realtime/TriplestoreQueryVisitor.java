package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;

import java.util.Iterator;

import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.BooleanOperator;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventFilter;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFetch;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.RelationalOperator;


/**
 * Generates "TriplestoreQuery" described in EPSPARQL20 grammar.
 * @author sobermeier
 *
 */
public class TriplestoreQueryVisitor extends GenericVisitor implements ElementVisitor, NodeVisitor{
	
	private String triplestoreQuery;
	private VarNameManager varNameManager;
	
	public TriplestoreQueryVisitor(VarNameManager varNameManager){
		triplestoreQuery = "";
		this.varNameManager = varNameManager;
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
	public Object visitLiteral(Node_Literal it, LiteralLabel lit) { //TODO Use prolog typesystem.
		triplestoreQuery += "'" + lit.getLexicalForm() + "', ";
		return lit;
	}

	@Override
	public Object visitURI(Node_URI it, String uri) {
		triplestoreQuery += "'" + uri + "', ";
		return uri;
	}

	@Override
	public Object visitVariable(Node_Variable it, String name) {
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
	public void visit(ElementNamedGraph el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementPathBlock el) {
		Iterator<TriplePath> iter = el.getPattern().getList().iterator();
		
		while (iter.hasNext()) {
			triplestoreQuery += "rdf(";
			TriplePath tmpTriplePath = iter.next();
			// Get data from one graph
			tmpTriplePath.getSubject().visitWith(this);
			tmpTriplePath.getPredicate().visitWith(this);
			tmpTriplePath.getObject().visitWith(this);
			triplestoreQuery += varNameManager.getTriplestoreVariable() + ")"; 
			
			if(iter.hasNext()){
				triplestoreQuery += ", ";
			}
		}
	}

	@Override
	public void visit(ElementFilter el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementAssign el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementBind el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementUnion el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementOptional el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementGroup el) {
		// Visit all group elements
		for(int i=0; i<el.getElements().size(); i++){
			el.getElements().get(i).visit(this); 
		}
	}

	@Override
	public void visit(ElementDataset el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementExists el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementNotExists el) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(ElementService el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementFetch el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementSubQuery el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementEventGraph el) {
		// Visit triples
		triplestoreQuery = "";
		el.getElement().visit(this);
	}

	@Override
	public void visit(ElementEventBinOperator el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementEventFilter el) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void visit(RelationalOperator arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(BooleanOperator arg0) {
		// TODO Auto-generated method stub
		
	}




	
}
