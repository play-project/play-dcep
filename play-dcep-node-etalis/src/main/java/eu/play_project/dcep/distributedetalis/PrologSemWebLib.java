package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.escapeForProlog;
import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;
import jpl.PrologException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.api.UsePrologSemWebLib;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class PrologSemWebLib implements UsePrologSemWebLib {
	private static JtalisContextImpl ctx;
	int oldValue = 0;
	long internalEventId = 0; // Ordered event ids. id_0 < id_1 < id2 ...
	private static Logger logger = LoggerFactory.getLogger(PrologSemWebLib.class);

	@Override
	public void init(JtalisContextImpl ctx) throws DistributedEtalisException {
		logger.debug("Initializing {}", PrologSemWebLib.class.getSimpleName());
		
		PrologSemWebLib.ctx = ctx;

		try {
			// Load SWI-Prolog Semantic Web Library
			logger.debug("Loading SWI-Prolog Semantic Web Library");
			ctx.getEngineWrapper().executeGoal("[library(semweb/rdf_db)]");
			ctx.getEngineWrapper().executeGoal("[library(xpath)]");
			ctx.getEngineWrapper().executeGoal("use_module(library(xpath))");
			ctx.getEngineWrapper().executeGoal("use_module(library(random))");
			ctx.getEngineWrapper().executeGoal("assert(gcDelay(2))");
			ctx.setEtalisFlags("garbage_clt", "on");
			ctx.setEtalisFlags("garbage_control","general");
			ctx.setEtalisFlags("save_ruleId", "on");
		} catch (PrologException e) {
			throw new DistributedEtalisException("Error loading SWI-Prolog libraries: " + e.getMessage());
		}
	}

	@Override
	public Boolean addEvent(CompoundEvent event)
			throws DistributedEtalisException {
		Boolean dataAddedToTriplestore = true;
		Boolean gcDataAdded = true;
		StringBuilder prologString = new StringBuilder();
		int i = 0;

		for (Quadruple quadruple : event) {
			i++;

			// TODO use prolog type system
			Node o = quadruple.getObject();
			String rdfObject;
			if (o.isLiteral()) {
				rdfObject = quoteForProlog(escapeForProlog(quadruple.getObject().getLiteralLexicalForm()));
			} else {
				// 5.) Resource URI
				rdfObject = quoteForProlog(o.getURI());
			}
			if (i > 1) {
				prologString.append(", ");
			}
			// Add data to triplestore
			prologString.append("rdf_assert(" + quoteForProlog(quadruple.getSubject().toString())
					+ ", " + quoteForProlog(quadruple.getPredicate().toString()) + ", "
					+ rdfObject + ", " + quoteForProlog(event.getGraph().toString()) + ")");
		}

		dataAddedToTriplestore = addPayloadToPlTriplestore(prologString.toString());

		// Add GC counter.
		gcDataAdded = ctx.getEngineWrapper().executeGoal(
				"assert(referenceCounter(" + quoteForProlog(event.getGraph().toString()) + ", "
						+ "2147483647" + ", -1))");

		if (!gcDataAdded) {
			throw new DistributedEtalisException(
					"Failed to insert garbage collection information in Prolog.");
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
			boolean result = ctx.getEngineWrapper().executeGoal(prologString);
			ctx.getEngineWrapper().executeGoal("write('a'),write(\\n).");
			ctx.getEngineWrapper().executeGoal("triggerEventWithDelay(virtualEvent2, 1).");
			ctx.getEngineWrapper().executeGoal("event(virtualEvent2).");
			System.out.println("aa");
//			ctx.getEngineWrapper().executeGoal("printRdfStat");
//			ctx.getEngineWrapper().executeGoal("printNumberOfEvents");
//			ctx.getEngineWrapper().executeGoal("printRefCountN");
//			ctx.getEngineWrapper().executeGoal("printReferenceCounters");
			
			return result;
		} catch (PrologException e) {
			if (e.getMessage().contains("error(permission_error(write, rdf_db, default)")) {
				logger.warn("Error: db is locked. Try again.");
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				return this.addPayloadToPlTriplestore(prologString);
			} else {
				logger.error("Error on new event.", e);
				return false;
			}
			
			
		}
	}
	@Override
	public CompoundEvent getRdfData(String complexEventID) {
		throw new RuntimeException("Not implemented in this class.");
	}
}
