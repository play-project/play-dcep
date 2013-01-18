package eu.play_project.dcep.distributedetalis;

import com.hp.hpl.jena.graph.Node;
import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.api.UsePrologSemWebLib;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class PrologSemWebLib implements UsePrologSemWebLib {
	private JtalisContextImpl ctx;
	int count = 0;
	int oldValue =0;
	
	@Override
	public void init(JtalisContextImpl ctx) {
		this.ctx = ctx;
		// Load SWI-Prolog Semantic Web Library
		ctx.getEngineWrapper().executeGoal("[library(semweb/rdf_db)]");
		ctx.getEngineWrapper().executeGoal("[library(xpath)]");
		ctx.getEngineWrapper().executeGoal("use_module(library(xpath))");
		ctx.getEngineWrapper().executeGoal("use_module(library(random))");
		ctx.setEtalisFlags("garbage_clt", "on");
		ctx.setEtalisFlags("garbage_control","general");
		ctx.setEtalisFlags("save_ruleId", "on");
	}

	@Override
	public Boolean addEvent(CompoundEvent event) throws Exception {
		Boolean dataAddedToTriplestore = true;
		Boolean gcDataAdded = true;	
		for(Quadruple quadruple : event.getQuadruples()){
			if((dataAddedToTriplestore && gcDataAdded)){
				// TODO use prolog type system
				Node o = quadruple.getObject();
				String rdfObject;
				if (o.isLiteral()) {
					rdfObject = "'" + quadruple.getObject().getLiteralValue() + "'";
					
					
//					if (null != o.getLiteralDatatypeURI()) {
//						// 1.) Numbers
//						if (o.getLiteralValue() instanceof XSDDouble || o.getLiteralValue() instanceof XSDFloat || o.getLiteralValue() instanceof XSDBaseNumericType) {
//							// Create native Prolog number (without quotes)
//							rdfObject = "literal(type('" + o.getLiteralDatatypeURI() + "', " + o.getLiteralLexicalForm() + "))";
//						}
//						// 2.) Other specified types
//						else {
//							// Create prolog atom
//							rdfObject = "literal(type('" + o.getLiteralDatatypeURI() + "', '" + o.getLiteralLexicalForm() + "'))";
//						}
//					}
//					// Language literals
//					else if (NodeUtils.hasLang(o)) {
//						// 3.) Create prolog atom
//						rdfObject = "literal(lang('" + o.getLiteralLanguage() + "', '" + o.getLiteralLexicalForm() + "'))";
//					}
//					// 4.) Plain literals
//					else {
//						rdfObject = "literal('" + o.getLiteralLexicalForm() + "')";
//					}
				}
				else {
					// 5.) Resource URI
					rdfObject = "'" + o + "'";
				}
				// Add data to triplestore
				dataAddedToTriplestore = ctx.getEngineWrapper().executeGoal("rdf_assert(" +
						"'" + quadruple.getSubject()   + "', "+
						"'" + quadruple.getPredicate() + "', "+
						    rdfObject                  + ", "+
						//"'" + quadruple.getObject()    + "', "+
						"'" + event.getGraph() + "')");
			} else {
				throw new Exception("Failed to insert event data in SWI Prolog triple store.");
			}
		}
		// Add GC counter.
		gcDataAdded = ctx.getEngineWrapper().executeGoal("assert(id('" + event.getGraph() + "', 0))");	 
		count++;
		return (dataAddedToTriplestore && gcDataAdded);
	}

	@Override
	public void removeEvent(String id) {
		ctx.getEngineWrapper().executeGoal("rdf_retractall(S,P,O,'" + id + "')");
		//etalis.getEngineWrapper().executeGoal("rdf_retractall(S,P,O,G)");
	}


	@Override
	public CompoundEvent getRdfData(String complexEventID) {
		throw new RuntimeException("Not implemented in this class.");
	}
}
