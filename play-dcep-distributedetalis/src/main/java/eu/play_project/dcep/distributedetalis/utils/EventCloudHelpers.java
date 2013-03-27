package eu.play_project.dcep.distributedetalis.utils;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;

import java.util.ArrayList;
import java.util.List;

import org.event_processing.events.types.Event;
import org.ontoware.rdf2go.impl.jena29.TypeConversion;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import com.hp.hpl.jena.vocabulary.RDF;

import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventTypeMetadata;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class EventCloudHelpers {

	public static final Quadruple toQuadruple(URI context, Resource subject,
	                                             URI predicate, Node object) {
	    return new Quadruple(
	            TypeConversion.toJenaNode(context),
	            TypeConversion.toJenaNode(subject),
	            TypeConversion.toJenaNode(predicate),
	            TypeConversion.toJenaNode(object));
	}

	public static final Quadruple toQuadruple(Statement stmt) {
	    return toQuadruple(
	            stmt.getContext(), stmt.getSubject(), stmt.getPredicate(),
	            stmt.getObject());
	}

	public static final CompoundEvent toCompoundEvent(Event event) {
		return toCompoundEvent(event.getModel());
	}

	public static final CompoundEvent toCompoundEvent(Model model) {
		List<Quadruple> quadruples = new ArrayList<Quadruple>();
		URI context = model.getContextURI();
		if (context != null) {
			for (Statement statement : model) {
				quadruples.add(toQuadruple(context, statement.getSubject(), statement.getPredicate(), statement.getObject()));
			}
		}
		else {
			throw new IllegalArgumentException("Event without graph name! All RDF graphs for events must have a graph name (context), i.e. must consist of RDF quadruples, not triples.");
		}
		
		return new CompoundEvent(quadruples);
	}

	/**
	 * Print the member event IDs (if present in the complex event) as
	 * a space-separated string. This method will be replaced when the
	 * :members feature becomes a first-class feature of DCEP.
	 * @param m
	 * @return
	 * @deprecated This will be removed when :members feature becomes a built-in
	 * feature of DCEP.
	 */
	@Deprecated
	public static String getMembers(CompoundEvent event) {
		String members = "";
		for (Quadruple quadruple : event) {
			if (quadruple.getPredicate().toString().equals(Event.MEMBERS.toString())) {
	    		String member = quadruple.getObject().toString();
	    		int endIndex = member.lastIndexOf(EVENT_ID_SUFFIX);
	    		if (endIndex > 0 ) {
	    			member = member.substring(0, endIndex);
	    		}
	    		members += member + " ";
			}
		}
		return members;
	}

	/**
	 * Print the member event IDs (if present in the complex event) as
	 * a space-separated string. This method will be replaced when the
	 * :members feature becomes a first-class feature of DCEP.
	 * @param m
	 * @return
	 * @deprecated This will be removed when :members feature becomes a built-in
	 * feature of DCEP.
	 */
	@Deprecated
	public static String getMembers(Model event) {
		String members = "";
		for (Statement statement : event) {
			if (statement.getPredicate().toString().equals(Event.MEMBERS.toString())) {
	    		String member = statement.getObject().toString();
	    		int endIndex = member.lastIndexOf(EVENT_ID_SUFFIX);
	    		if (endIndex > 0 ) {
	    			member = member.substring(0, endIndex);
	    		}
	    		members += member + " ";
			}
		}
		return members;
	}

	/**
	 * Returns the RDF event type as URI string. This method tries to find {@code rdf:type}
	 * statements with the proper event ID as subject. If this fails it falls back to arbitrary
	 * {@code rdf:type} statements in the event and finally defaults to the basic event type of
	 * {@linkplain Event.RDFS_CLASS}
	 * 
	 * @see EventTypeMetadata#getEventType(Model)
	 */
	public static String getEventType(CompoundEvent event) {
		com.hp.hpl.jena.graph.Node primaryType = null;
		com.hp.hpl.jena.graph.Node secondaryType = null;
		
		if (event.getGraph() != null) {
			com.hp.hpl.jena.graph.Node eventId = com.hp.hpl.jena.graph.Node.createURI(event.getGraph() + EVENT_ID_SUFFIX);
			for (Quadruple quad : event) {
				if (quad.getPredicate().equals(RDF.type.asNode())) {
					secondaryType = quad.getObject();
					if (quad.getSubject().equals(eventId)) {
						primaryType = quad.getObject();
						break;
					}
				}
			}
		}
		
		if (primaryType != null) {
			return primaryType.toString();
		}
		else if (secondaryType != null) {
			return secondaryType.toString();
		}
		else {
			return Event.RDFS_CLASS.toString();
		}
	}
	
	/**
	 * Returns the event cloud ID which is contained for a given event.
	 */
	public static String getCloudId(CompoundEvent event) {
		com.hp.hpl.jena.graph.Node primaryType = null;
		com.hp.hpl.jena.graph.Node secondaryType = null;

		String streamId = "";
		String cloudId = "";
		
		if (event.getGraph() != null) {
			com.hp.hpl.jena.graph.Node eventId = com.hp.hpl.jena.graph.Node.createURI(event.getGraph() + EVENT_ID_SUFFIX);
			for (Quadruple quad : event) {
				if (quad.getPredicate().toString().equals(Event.STREAM.toString())) {
					secondaryType = quad.getObject();
					if (quad.getSubject().equals(eventId)) {
						primaryType = quad.getObject();
						break;
					}
				}
			}
		}
		
		if (primaryType != null) {
			streamId = primaryType.toString();
			cloudId = streamId.substring(0,
					streamId.lastIndexOf(Stream.STREAM_ID_SUFFIX));
			return cloudId;
		}
		else if (secondaryType != null) {
			streamId = secondaryType.toString();
			cloudId = streamId.substring(0,
					streamId.lastIndexOf(Stream.STREAM_ID_SUFFIX));
			return cloudId;
		}
		else {
			return cloudId;
		}
	}
}
