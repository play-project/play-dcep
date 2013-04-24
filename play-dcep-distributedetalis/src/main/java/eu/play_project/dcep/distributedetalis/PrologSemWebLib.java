package eu.play_project.dcep.distributedetalis;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.UsePrologSemWebLib;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class PrologSemWebLib implements UsePrologSemWebLib {
	private static JtalisContextImpl ctx;
	int oldValue =0;
	long internalEventId = 0; // Ordered event ids. id_0 < id_1 < id2 ...
	
	@Override
	public void init(JtalisContextImpl ctx) {
		PrologSemWebLib.ctx = ctx;
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
	public Boolean addEvent(CompoundEvent event) throws DistributedEtalisException {
		Boolean dataAddedToTriplestore = true;
		Boolean gcDataAdded = true;
		for(Quadruple quadruple : event){
			if(dataAddedToTriplestore){
				// TODO use prolog type system
				Node o = quadruple.getObject();
				String rdfObject;
				if (o.isLiteral()) {
					rdfObject = "'" + escapeForProlog(quadruple.getObject().getLiteralLexicalForm()) + "'";
					
					
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
					rdfObject = "'" + o.getURI() + "'";
				}
				// Add data to triplestore
				String prologString = "rdf_assert(" +
						"'" + quadruple.getSubject()   + "', " +
						"'" + quadruple.getPredicate() + "', " +
						    rdfObject                  + ", " +
						"'" + event.getGraph() + "')";
				dataAddedToTriplestore = ctx.getEngineWrapper().executeGoal(prologString);
			} else {
				throw new DistributedEtalisException("Failed to insert event data in Prolog triple store.");
			}
		}
		// Add GC counter.
		gcDataAdded = ctx.getEngineWrapper().executeGoal("assert(referenceCounter('" + event.getGraph() + "', " + internalEventId + ", -1))");
		internalEventId++;
		
		if (!gcDataAdded) {
			throw new DistributedEtalisException("Failed to insert garbage collection information in Prolog.");
		}
		
		return (dataAddedToTriplestore && gcDataAdded);
}

	@Override
	public void removeEvent(String id) {
		ctx.getEngineWrapper().executeGoal("rdf_retractall(S,P,O,'" + id + "')");
	}


	@Override
	public CompoundEvent getRdfData(String complexEventID) {
		throw new RuntimeException("Not implemented in this class.");
	}
	
	/**
	 * Escape all characters which are illegal in Prolog's quoted strings:
	 * {@code It's me, Mario.} becomes {@code It\'s me, Mario.}. The resulting
	 * strings are meanto to be used as <i>quoted atoms</i> in Prolog, between
	 * single quotes.
	 * 
	 * @see http
	 *      ://www.swi-prolog.org/pldoc/doc_for?object=section%284,%272.15.1.2
	 *      %27,swi%28%27/doc/Manual/syntax.html%27%29%29
	 */
	public static String escapeForProlog(String s) {
		return s.replaceAll("'", "\'");
	}
}
