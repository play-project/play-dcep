package eu.play_project.dcep.distributedetalis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jpl.PrologException;

import com.hp.hpl.jena.graph.Node;
import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.UsePrologSemWebLib;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class PrologSemWebLib implements UsePrologSemWebLib {
	private static JtalisContextImpl ctx;
	int oldValue =0;
	long internalEventId = 0; // Ordered event ids. id_0 < id_1 < id2 ...
	private static Logger logger = LoggerFactory.getLogger(PrologSemWebLib.class);
	int triesToStoreData = 0;
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
				triesToStoreData = 0;
				dataAddedToTriplestore = addPayloadToPlTriplestore(prologString);
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

	/**
	 * Execute given String in prolog. If it was not possible retry it after 1ms.
	 * @param prologString code to execute in prolog.
	 * @return true if code was executed.
	 */
	private boolean addPayloadToPlTriplestore(String prologString){
		try{
			return ctx.getEngineWrapper().executeGoal(prologString);
		}catch (PrologException e) {
			if(e.getMessage().contains("error(permission_error(write, rdf_db, default)")){
				logger.error("Error db is blocked. Try againe. Count: ", triesToStoreData);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				return this.addPayloadToPlTriplestore(prologString);
			}else{
				logger.error("Error on new event.", e);
				return false;
			}
			
			
		}
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
