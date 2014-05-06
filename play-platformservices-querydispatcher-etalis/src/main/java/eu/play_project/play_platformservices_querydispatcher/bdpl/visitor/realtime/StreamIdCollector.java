package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotOperator;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;

import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_platformservices.api.QueryDetails;

/**
 * Enriches a {@linkplain QueryDetails} object with information about the streams in a query.
 * 
 * @author sobermeier
 * @author stuehmer
 */
public class StreamIdCollector {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void getStreamIds(Query query, QueryDetails qd) {
		if (qd == null) {
			throw new RuntimeException("Parameter QueryDetails is null");
		} else {
			qd.setOutputStream(getOutputStream(query));
			qd.setInputStreams(getInputStreams(query));
			qd.setHistoricStreams(getHistoricStreams(query));
		}

	}

	/**
 	 * Returns the stream ID without {@code #stream} suffix to be used with EC and DSB.
 	 * 
	 * @param query
	 * @return
	 */
	private String getOutputStream(Query query) {
		TypeCheckVisitor v = new TypeCheckVisitor();

		boolean streamIdFound = false;
		Iterator<Triple> iter = query.getConstructTemplate().getTriples().iterator();
		Triple triple;

		while (iter.hasNext() && !streamIdFound) {
			triple = iter.next();
			if ((Boolean)triple.getPredicate().visitWith(v)) {
				if (triple.getObject().isURI()) {
					String streamId = triple.getObject().toString();
					return Stream.toTopicUri(streamId);
				} else {
					throw new RuntimeException("Output stream Id is not a URI or Literal");
				}
			}
		}
		return null;
	}

	/**
	 * Returns the input stream IDs without the {@code #stream} suffix to be used with EC and DSB.
	 * 
	 * @param query
	 * @return
	 */
	private Set<String> getInputStreams(Query query) {
		Set<String> streams = new HashSet<String>();
		ValueOrganizerVisitor valueOrganizerVisitor = new ValueOrganizerVisitor();

		query.getEventQuery().visit(valueOrganizerVisitor);
		if (valueOrganizerVisitor.getStreamURIs() != null) {
			Set<String> streamIds = valueOrganizerVisitor.getStreamURIs();
			for (String id : streamIds) {
				streams.add(Stream.toTopicUri(id));
			}
		}
		
		return streams;
	}
	

	/**
	 * Returns the historic stream IDs without the {@code #stream} suffix to be used with EC and DSB.
	 * 
	 * @param query
	 * @return
	 */
	private Set<String> getHistoricStreams(Query query) {
		Set<String> streams = null;
		ValueOrganizerVisitor valueOrganizerVisitor = new ValueOrganizerVisitor();

		Element element = query.getQueryPattern(); //Historic query.
			if (element !=  null) {
				
				element.visit(valueOrganizerVisitor);
				if (valueOrganizerVisitor.getStreamURIs().size() > 0) {
					streams = new HashSet<String>();
					for (String stream : valueOrganizerVisitor.getStreamURIs()) {
						streams.add(Stream.toTopicUri(stream));
					}
				}
			} else {
				logger.debug("No historic part in query to collect stream id from.");
			}
		return streams;
	}

	// Return value of URI element and traverse form ElementPathBlock to URI
	// element.
	private class ValueOrganizerVisitor extends GenericVisitor {
		Set<String> streamURIs;

		public ValueOrganizerVisitor(){
			streamURIs =  new HashSet<String>();
		}
		
		@Override
		public void visit(ElementPathBlock el) {
			TypeCheckVisitor v = new TypeCheckVisitor();
			for (TriplePath tmpTriplePath : el.getPattern().getList()) {
				// Check if type is ok
				if ((Boolean)tmpTriplePath.getPredicate().visitWith(v)) {
					if (tmpTriplePath.getObject().isURI()) {
						streamURIs.add(tmpTriplePath.getObject().toString());
						break;
					} else {
						throw new RuntimeException("Input stream Id is not a URI or Literal");
					}
				}
			}
		}

		@Override
		public void visit(ElementNamedGraph el){
			el.getElement().visit(this);
		}
		
		public Set<String> getStreamURIs() {
			return streamURIs;
		}

		@Override
		public void visit(ElementEventGraph el) {
			el.getElement().visit(this);
		}

		@Override
		public void visit(ElementGroup el) {
			// Visit all group elements
			for (Element element : el.getElements()) {
				element.visit(this);
			}
		}
		
		@Override
		public void visit(ElementEventBinOperator el) {
			el.getLeft().visit(this);
			el.getRight().visit(this);
		}
		
		@Override
		public void visit(ElementNotOperator el) {
			el.getStart().visit(this);
			el.getEnd().visit(this);
			el.getNot().visit(this);
		}
		
		@Override
		public void visit(ElementService el) {
			if(el.getServiceNode().isURI()) {
				streamURIs.add(el.getServiceNode().toString());
			} else {
				throw new RuntimeException("Input stream Id " + el.getServiceNode().toString() + " is not a URI.");
			}
		}
	}
	
	
	// Test if the type is http://events.event-processing.org/types/stream
	private class TypeCheckVisitor extends GenericVisitor {

		@Override
		public Object visitURI(Node_URI it, String uri) {

			if (uri.equals(org.event_processing.events.types.Event.STREAM.toString()) || uri.equals((org.event_processing.events.types.Event.STREAM + "/"))) {
				return new Boolean(true);
			}
			return new Boolean(false);
		}
	}	
}
