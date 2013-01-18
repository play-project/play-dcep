package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;


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


public class BinOperatorVisitor extends GenericVisitor implements ElementVisitor{
	String binOperator;
	
	public String getBinOperator(){
		return binOperator;
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementEventBinOperator el) {
		binOperator = "'" + el.getTyp() + "'";
	}

	@Override
	public void visit(ElementEventFilter el) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void visit(RelationalOperator relationalOperator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BooleanOperator booleanOperator) {
		// TODO Auto-generated method stub
		
	}

	

}
