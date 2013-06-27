package eu.play_project.dcep.distributedetalis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.impl.StatementImpl;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;

import eu.play_project.dcep.distributedetalis.utils.DsbHelpers;
import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
import eu.play_project.play_eventadapter.AbstractReceiver;
import eu.play_project.play_eventadapter.NoRdfEventException;
import fr.inria.eventcloud.api.CompoundEvent;

class EcConnectionListenerVirtuoso implements INotificationConsumer, Serializable {

	private static final long serialVersionUID = 100L;
	private DistributedEtalis dEtalis;
	private final AbstractReceiver rdfReceiver;
	private final Logger logger;
	
	public EcConnectionListenerVirtuoso(AbstractReceiver rdfReceiver) {
		this.rdfReceiver = rdfReceiver;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public void notify(Notify notify) throws WsnbException {
		if (this.dEtalis == null) {
			String msg = "Detalis was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		if (this.dEtalis.getEcConnectionManager() == null) {
			String msg = "ecConnectionManager was not set in " + this.getClass().getSimpleName();
			throw new IllegalStateException(msg);
		}
		
	    try {
	    	Model rdf = this.rdfReceiver.parseRdf(notify);
	    	deanonymize(rdf);
	    	CompoundEvent event = EventCloudHelpers.toCompoundEvent(rdf);
	    	String topic = DsbHelpers.topicToUri(notify.getNotificationMessage().get(0).getTopic());
	    	logger.debug("Received event {} on topic {} from the DSB.", event.getGraph(), topic);
	    	
		    // Forward the event to Detalis:
		    this.dEtalis.publish(event);
		    
		    // Store the event in Virtuoso:
		    ((EcConnectionManagerVirtuoso)this.dEtalis.getEcConnectionManager()).putDataInCloud(event, topic);
		    
	    } catch (NoRdfEventException e) {
			logger.error("Received a non-RDF event from the DSB: " + e.getMessage());
		}
	}

	public void setDetalis(DistributedEtalis dEtalis) {
		this.dEtalis = dEtalis;
	}
	
	/**
	 * Replace all BlankNodes as Subjects or Objects from the Statements in the
	 * given RDF2Go model.
	 * 
	 * @author Max Voelkel
	 * 
	 * @param m de-anonymise this model
	 */
	public static void deanonymize(Model m) {
		Iterator<Statement> it = m.iterator();
		long counter = 0;
		Map<BlankNode,URI> replacement = new HashMap<BlankNode,URI>();
		Set<Statement> badStatements = new HashSet<Statement>();
		Set<Statement> goodStatements = new HashSet<Statement>();
		while(it.hasNext()) {
			Statement s = it.next();
			org.ontoware.rdf2go.model.node.Resource subject = s.getSubject();
			if(s.getSubject() instanceof BlankNode) {
				badStatements.add(s);
				subject = toURI((BlankNode)subject, replacement, counter);
			}
			Node object = s.getObject();
			if(object instanceof BlankNode) {
				badStatements.add(s);
				object = toURI((BlankNode)object, replacement, counter);
			}
			goodStatements.add(new StatementImpl(m.getContextURI(), subject, s.getPredicate(),
			        object));
		}
		for(Statement s : badStatements)
			m.removeStatement(s);
		for(Statement s : goodStatements)
			m.addStatement(s);
		
	}
	
	/**
	 * Generate a unique URI for a BlankNode and put the BlankNode/URI pair into
	 * the given map.
	 * 
	 * @author Max Voelkel
	 * 
	 * @param blankNode - generate a URI for this BlankNode
	 * @param replacement - Map of BlankNode/URI pairs
	 * @param counter
	 * @return URI generated for the BlankNode
	 */
	public static URI toURI(BlankNode blankNode, Map<BlankNode,URI> replacement, long counter) {
		URI result = replacement.get(blankNode);
		if(result == null)
			result = new URIImpl("blank://" + (counter + 1));
		// TODO BlankNode identity might be too weak
		replacement.put(blankNode, result);
		return result;
	}
}