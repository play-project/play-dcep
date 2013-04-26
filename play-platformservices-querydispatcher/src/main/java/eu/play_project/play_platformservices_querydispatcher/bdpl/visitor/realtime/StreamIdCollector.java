package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_platformservices.api.QueryDetails;

/**
 * Enriches a {@linkplain QueryDetails} object with information about the streams in a query.
 * 
 * @author sobermeier
 * @author stuehmer
 */
public class StreamIdCollector {

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
		UriValueVisitor valueVisitor = new UriValueVisitor();

		boolean streamIdFound = false;
		Iterator<Triple> iter = query.getConstructTemplate().getTriples().iterator();
		Triple triple;

		while (iter.hasNext() && !streamIdFound) {
			triple = iter.next();
			if (triple.getPredicate().visitWith(v) != null) {
				if (triple.getObject().visitWith(valueVisitor) == null) {
					throw new RuntimeException("Output stream Id is not a URI or Literal");
				} else {
					String streamId = (String) triple.getObject().visitWith(valueVisitor);
					return Stream.toTopicUri(streamId);
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

		for (Element element : query.getEventQuery()) {
			element.visit(valueOrganizerVisitor);
			if (valueOrganizerVisitor.getStreamURI() != null) {
				String streamId = valueOrganizerVisitor.getStreamURI();
				streams.add(Stream.toTopicUri(streamId));
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
		Set<String> streams = new HashSet<String>();
		ValueOrganizerVisitor valueOrganizerVisitor = new ValueOrganizerVisitor();

		for (Element element : query.getHistoricQuery()) {
			element.visit(valueOrganizerVisitor);
			if (valueOrganizerVisitor.getStreamURI() != null) {
				String streamId = valueOrganizerVisitor.getStreamURI();
				streams.add(Stream.toTopicUri(streamId));
			}
		}

		return streams;
	}

	// Return value of URI elment and travers form ElementPathBlock to URI
	// elment.
	private class ValueOrganizerVisitor extends GenericVisitor {
		boolean elementContainsInputStream = false;
		String ok = "OK";
		String streamURI;

		@Override
		public void visit(ElementPathBlock el) {
			Iterator<TriplePath> iter = el.getPattern().getList().iterator();
			TypeCheckVisitor v = new TypeCheckVisitor();
			UriValueVisitor valueVisotor = new UriValueVisitor();
			for (TriplePath tmpTriplePath : el.getPattern().getList()) {
				streamURI = null;
				// Check if type is ok
				if (tmpTriplePath.getPredicate().visitWith(v) != null) {
					if (tmpTriplePath.getObject().visitWith(valueVisotor) == null) {
						throw new RuntimeException("Input stream Id is not a URI or Literal");
					} else {
						streamURI = (String) tmpTriplePath.getObject().visitWith(valueVisotor);
						break;
					}
				}
			}
		}

		public String getStreamURI() {
			return streamURI;
		}

		@Override
		public void visit(ElementEventGraph el) {
			el.getElement().visit(this);
		}

		@Override
		public void visit(ElementGroup el) {
			// Visit all group elements
			for (int i = 0; i < el.getElements().size(); i++) {
				el.getElements().get(i).visit(this);
			}
		}
	}

	// Test if the type is http://events.event-processing.org/types/stream
	private class TypeCheckVisitor extends GenericVisitor {
		private final String ok = "OK";

		@Override
		public Object visitURI(Node_URI it, String uri) {

			if (uri.equals(org.event_processing.events.types.Event.STREAM.toString()) || uri.equals((org.event_processing.events.types.Event.STREAM + "/"))) {
				return ok;
			}
			return null;
		}
	}

	// Test if the type is http://events.event-processing.org/types/stream
	private class UriValueVisitor extends GenericVisitor {

		@Override
		public Object visitURI(Node_URI it, String uri) {
			return uri;
		}
	}
}
