package eu.play_project.dcep.distributedetalis;

import static eu.play_project.dcep.constants.DcepConstants.LOG_DCEP_FAILED_EXIT;
import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.unquoteFromProlog;
import static eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601;
import static eu.play_project.play_commons.constants.Event.EVENT_ID_PLACEHOLDER;
import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static eu.play_project.play_commons.constants.Namespace.EVENTS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.event_processing.events.types.Event;
import org.ontoware.rdf2go.impl.jena.TypeConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.jtalis.core.event.EtalisEvent;
import com.jtalis.core.event.JtalisOutputEventProvider;

import eu.play_project.dcep.api.SimplePublishApi;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.HistoricalDataEngine;
import eu.play_project.dcep.distributedetalis.api.VariableBindings;
import eu.play_project.dcep.distributedetalis.join.Engine;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import eu.play_project.play_commons.constants.Source;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class JtalisOutputProvider implements JtalisOutputEventProvider, Serializable {

	private static final long serialVersionUID = 100L;
	private static Logger logger = LoggerFactory.getLogger(JtalisOutputProvider.class);
	
	MeasurementUnit measurementUnit;

	boolean shutdownEtalis = false; // If true ETALIS will shutdown.

	private final PlayJplEngineWrapper engine;
	private final Set<SimplePublishApi> recipients;
	private final Map<String, BdplQuery> registeredQueries;
	private final HistoricalDataEngine historicData;
	
	private final static Node STARTTIME = TypeConversion.toJenaNode(Event.STARTTIME);
	private final static Node ENDTIME = TypeConversion.toJenaNode(Event.ENDTIME);
	private final static Node EVENTPATTERN = TypeConversion.toJenaNode(Event.EVENTPATTERN);
	private final static Node SOURCE = TypeConversion.toJenaNode(Event.SOURCE);
	
	private final static String PATTERN_BASE_URI = DcepConstants.getProperties().getProperty("platfomservices.querydispatchapi.rest");

	public JtalisOutputProvider(Set<SimplePublishApi> recipients, Map<String, BdplQuery> registeredQueries, EcConnectionManager ecConnectionManager, MeasurementUnit measurementUnit) {
		this.engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		this.recipients = recipients;
		this.registeredQueries = registeredQueries;
		this.historicData = new Engine(ecConnectionManager);
		this.measurementUnit = measurementUnit;
	}
	
	@Override
	public void setup() {
	}

	@Override
	public void shutdown() {
		shutdownEtalis = true;
	}

	@Override
	public void outputEvent(EtalisEvent event) {

		try {
			List<Quadruple> quadruples = this.getEventData(engine, event);
					 
			// Publish complex event
			CompoundEvent result = new CompoundEvent(quadruples);

			measurementUnit.eventProduced(result, event.getName());
			
			if(recipients.size() < 1) {
				logger.warn(LOG_DCEP_FAILED_EXIT + "No recipients for complex events.");
			}
			
			for (SimplePublishApi recipient : recipients) {
				recipient.publish(result);
			}
		} catch (RetractEventException e) {
			logger.info(LOG_DCEP_FAILED_EXIT + "Retract ... an event was not created because its historic part was not fulfilled.");
		} catch (Exception e) {
			logger.error(LOG_DCEP_FAILED_EXIT + "Exception appeared: {}" + e.getMessage(), e);
		}
	}
	
	/**
	 * Get event data from Prolog and EventCloud.
	 */
	public List<Quadruple> getEventData(PlayJplEngineWrapper engine, EtalisEvent event) throws RetractEventException {
		List<Quadruple> quadruples = new ArrayList<Quadruple>();
		String eventId = EVENTS.getUri() + event.getProperty(0).toString();
	
		final Node GRAPHNAME = NodeFactory.createURI(eventId);
		final Node EVENTID = NodeFactory.createURI(eventId + EVENT_ID_SUFFIX);

		/*
		 *  Add implicit values from Jtalis to each event:
		 */
		quadruples.add(new Quadruple(
				GRAPHNAME,
				EVENTID,
				EVENTPATTERN,
				//Node.createURI(DcepConstants.getProperties().getProperty("platfomservices.querydispatchapi.rest") + event.getRuleID()))); // FIXME sobermeier
				NodeFactory.createURI(PATTERN_BASE_URI + event.getStringProperty(1))));

		quadruples.add(new Quadruple(
				GRAPHNAME,
				EVENTID,
				STARTTIME,
				NodeFactory.createLiteral(
						DateFormatUtils.format(event.getTimeStarts(), DATE_FORMAT_8601),
						XSDDatatype.XSDdateTime)));

		quadruples.add(new Quadruple(
				GRAPHNAME,
				EVENTID,
				ENDTIME,
				NodeFactory.createLiteral(
						DateFormatUtils.format(event.getTimeEnds(), DATE_FORMAT_8601),
						XSDDatatype.XSDdateTime)));

		quadruples.add(new Quadruple(
				GRAPHNAME,
				EVENTID,
				SOURCE,
				NodeFactory.createURI(Source.Dcep.toString())));
		
		logger.debug("(1/3) static quads :\n{}", quadruples);
		
		/*
		 * Add payload data to event:
		 */
		Hashtable<String, Object>[] triples =  engine.getTriplestoreData(event.getStringProperty(0));
		
		if (triples.length < 1) {
			logger.warn("No event attributes (triples) were returned from Etalis for event '{}'", eventId);
		}

		for(Hashtable<String, Object> item : triples) {
			// Remove single quotes around Prolog strings
			String subject = item.get("S").toString();
			subject = subject.substring(1, subject.length() - 1);
			String predicate = item.get("P").toString();
			predicate = predicate.substring(1, predicate.length() - 1);
			String object = unquoteFromProlog(item.get("O").toString());
			Node objectNode = EventHelpers.toJenaNode(object);
			
			quadruples.add(new Quadruple(
					GRAPHNAME,
					// Replace dummy event id placeholder with actual unique id for complex event:
					(subject.equals(EVENT_ID_PLACEHOLDER) ? EVENTID : NodeFactory.createURI(subject)),
					NodeFactory.createURI(predicate),
	                objectNode));
		}

		logger.debug("(2/3) static quads, prolog quads:\n{}", quadruples);

		/*
		 * Add historic data to event:
		 */
		BdplQuery query = this.registeredQueries.get(event.getProperties()[1].toString());
		if (query == null) {
			logger.error("Query with ID {} was not found in registeredQueries.", event.getProperties()[1].toString());
		} else if (query.getHistoricalQueries() != null && !query.getHistoricalQueries().isEmpty()) {
			
			
			// Check if shared variables exists.
			boolean sharedVariablesExists =  false;
			for (HistoricalQuery hQuery : query.getHistoricalQueries()) {
				if(hQuery.hasSharedVariablesWithRealtimePart()){
					sharedVariablesExists = true;
				}
			}
			
			//Get variable bindings.
			VariableBindings variableBindings;
			if(sharedVariablesExists) {
				variableBindings = JtalisOutputProvider.getSharedVariablesValues(engine, event.getProperties()[0].toString());
			} else {
				variableBindings = new VariableBindings();
			}
			logger.debug("PROLOG VALUES: {}", variableBindings);

			
			//Get historical data to the given binding.
			HistoricalData values = this.historicData.get(query.getHistoricalQueries(), variableBindings);
			logger.debug("HISTORIC VALUES: {}", values);

			if (values.isEmpty()) {
				// there is no matching historic data so the event pattern is not fulfilled:
				throw new RetractEventException();
			} else {
				quadruples.addAll(query.getConstructTemplate().fillTemplate(values, GRAPHNAME, EVENTID));
				logger.debug("(3/3) static quads, prolog quads, historic quads:\n{}", quadruples);
			}
		}

		return quadruples;
	}
	
	public static VariableBindings getSharedVariablesValues(PlayJplEngineWrapper engine, String complexEventId) {
		// HashMap with values of variables.
		VariableBindings variableValues = new VariableBindings();

		try {
			// Get variables and values
			Hashtable<String, Object>[] result = engine.execute("variableValues(" + complexEventId + ", VarName, VarValue)");
			
			// Delete data in prolog engine.
			engine.execute("variabeValuesDel(" + complexEventId + ")");

			// Get all values of a variable
			for (Hashtable<String, Object> resultTable : result) {
				String varName = resultTable.get("VarName").toString();
				String varValue = resultTable.get("VarValue").toString();

				// Prepare list
				if (!variableValues.containsKey(varName)) {
					variableValues.put(varName, new LinkedList<Object>());
				}

				// Add new value to list
				if (varValue != null && !varValue.isEmpty()) {
					variableValues.get(varName).add(unquoteFromProlog(varValue));
				}
			}
		} catch (Exception e) {
			logger.debug("No Variable results", e);
		}
		
		return variableValues;
	}

}
