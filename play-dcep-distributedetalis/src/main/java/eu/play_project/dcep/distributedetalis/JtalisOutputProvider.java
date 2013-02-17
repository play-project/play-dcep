package eu.play_project.dcep.distributedetalis;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_PLACEHOLDER;
import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static eu.play_project.play_commons.constants.Namespace.EVENTS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.event_processing.events.types.Event;
import org.ontoware.rdf2go.impl.jena29.TypeConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.jtalis.core.event.EtalisEvent;
import com.jtalis.core.event.JtalisOutputEventProvider;

import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.HistoricalData;
import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import eu.play_project.dcep.distributedetalis.join.Engine;
import eu.play_project.play_commons.constants.Source;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class JtalisOutputProvider implements JtalisOutputEventProvider, Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(JtalisOutputProvider.class);

	boolean shutdownEtalis = false; // If true ETALIS will shutdown.

	private PlayJplEngineWrapper engine;
	private Set<SimplePublishApi> recipients;
	private Map<String, EpSparqlQuery> registeredQueries;
	private HistoricalData historicData;
	
	private final static Node STARTTIME = TypeConversion.toJenaNode(Event.STARTTIME);
	private final static Node ENDTIME = TypeConversion.toJenaNode(Event.ENDTIME);
	private final static Node EVENTPATTERN = TypeConversion.toJenaNode(Event.EVENTPATTERN);
	private final static Node SOURCE = TypeConversion.toJenaNode(Event.SOURCE);
	
	private static final String DATE_FORMAT_8601 = eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601;
	                                               

	public JtalisOutputProvider(Set<SimplePublishApi> recipients, Map<String, EpSparqlQuery> registeredQueries, EcConnectionManager ecConnectionManager) {
		this.engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		this.recipients = recipients;
		this.registeredQueries = registeredQueries;
		this.historicData = new Engine(ecConnectionManager);
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
		//FIXME sobermeier: separate measurement events (e.g. without using "complex")

		try {
			List<Quadruple> quadruples = this.getEventData(engine, event);
		
			// Publish complex event
			CompoundEvent result = new CompoundEvent(quadruples);
		
			//measurementUnit.eventProduced(result, event.getProperties()[1].toString());
			// event.getRuleID(); //TODO sobermeier use this.
	
			logger.info("DCEP Exit " + result.getGraph() + " " + EventCloudHelpers.getMembers(result));
			if(recipients.size()<1) logger.warn("No recipient for complex events.");
			
			for (SimplePublishApi recipient : recipients) {
				recipient.publish(result);
			}
		} catch (RetractEventException e) {
			logger.info("DCEP Retract ... an event was not created because its historic part was not fulfilled." );
		}
	}
	
	/**
	 * Get event data from Prolog and Event Cloud.
	 */
	public List<Quadruple> getEventData(PlayJplEngineWrapper engine, EtalisEvent event) throws RetractEventException {
		List<Quadruple> quadruples = new ArrayList<Quadruple>();
		
		String eventId = EVENTS.getUri() + event.getStringProperty(0); 
	
		final Node GRAPHNAME = Node.createURI(eventId);
		final Node EVENTID = Node.createURI(eventId + EVENT_ID_SUFFIX);

		/*
		 *  Add implicit values from Jtalis to each event:
		 */
		quadruples.add(new Quadruple(
				GRAPHNAME, 
				EVENTID, 
				EVENTPATTERN, 
				//Node.createURI(event.getRuleID()))); // FIXME sobermeier
				Node.createURI(event.getStringProperty(1))));

		quadruples.add(new Quadruple(
				GRAPHNAME,
				EVENTID,
				STARTTIME,
				Node.createLiteral(
						DateFormatUtils.format(event.getTimeStarts(), DATE_FORMAT_8601),
						XSDDatatype.XSDdateTime)));

		quadruples.add(new Quadruple(
				GRAPHNAME,
				EVENTID,
				ENDTIME,
				Node.createLiteral(
						DateFormatUtils.format(event.getTimeEnds(), DATE_FORMAT_8601),
						XSDDatatype.XSDdateTime)));

		quadruples.add(new Quadruple(
				GRAPHNAME, 
				EVENTID, 
				SOURCE, 
				Node.createURI(Source.Dcep.toString())));

		//TODO Add :members to the event (an RDF list of all simple events which were detected)
		
		/*
		 * Add payload data to event:
		 */
		Hashtable<String, Object>[] triples =  engine.getTriplestoreData(event.getStringProperty(0));

		for(Hashtable<String, Object> item : triples) {
			// Remove single quotes around Prolog strings
			String subject = item.get("S").toString();
			subject = subject.substring(1, subject.length() - 1);
			String predicate = item.get("P").toString();
			predicate = predicate.substring(1, predicate.length() - 1);
			String object = item.get("O").toString();
			if (object.startsWith("'") && object.endsWith("'")) {
				object = object.substring(1, object.length() - 1);
			}

			Node objectNode = EventHelpers.toJenaNode(object);
			
			quadruples.add(new Quadruple(
					GRAPHNAME,
					// Replace dummy event id placeholder with actual unique id for complex event:
					(subject.equals(EVENT_ID_PLACEHOLDER) ? EVENTID : Node.createURI(subject)),
	                Node.createURI(predicate), 
	                objectNode));
		}
		
		/*
		 * Add historic data to event:
		 */
		EpSparqlQuery query = this.registeredQueries.get(event.getProperties()[1].toString());
		if (query == null) {
			logger.error("Query with ID {} was not found in registeredQueries.", event.getProperties()[1].toString());
		} else if (query.gethistoricalQueries() != null && !query.gethistoricalQueries().isEmpty()) {
			Map<String, List<String>> variableBindings = JtalisOutputProvider
					.getSharedVariablesValues(engine, event.getProperties()[1].toString());
			System.out.println("Debug =========================================================="); 
			//Test//////////////////////
			List<String> bValues =  new LinkedList<String>();
			bValues.add("bob1");
			bValues.add("bob2");
			variableBindings.put("e4", bValues);
			// Test -----------------------

			//Print variable bindings
			for (String variable : variableBindings.keySet()) {
				System.out.print(variable);
				System.out.println(variableBindings.get(variable));
			}
			
			System.out.println("Historical queries");
			List<String> var1 =  new LinkedList<String>();
			var1.add("alice1");
			 query.gethistoricalQueries().get(0).setVariables(var1) ;
						
			System.out.println("Debug =========================================================="); 
			Map<String, List<String>> values = this.historicData.get(query.gethistoricalQueries(), variableBindings);

			if (values.isEmpty()) {
				throw new RetractEventException();
			} else {
				String vars = "";
				for (String varName : values.keySet()) {
					vars += " " + varName;
				}
				logger.info("SHARED VARIABLES: " + vars);
				query.getConstructTemplate();
				query.getConstructTemplate().fillTemplate(variableBindings, GRAPHNAME, EVENTID);
				quadruples.addAll(query.getConstructTemplate().fillTemplate(variableBindings, GRAPHNAME, EVENTID));
			}
		}
		
		//Remove unused triples from prolog
		engine.collectGarbage();
		
		return quadruples;
	}
	
	public static Map<String, List<String>> getSharedVariablesValues(PlayJplEngineWrapper engine, String queryId) {
		// HashMap with values of variables.
		Map<String, List<String>> variableValues = new HashMap<String, List<String>>();

		try {
			// Get variables and values
			Hashtable<String, Object>[] result = engine.execute("variableValues(" + queryId + ", VarName, VarValue)");

			// Get all values of a variable
			for (Hashtable<String, Object> resultTable : result) {
				if (!variableValues.containsKey(resultTable.get("VarName").toString())) {
					variableValues.put(resultTable.get("VarName").toString(), new ArrayList<String>());
				}

				// Add new value to list.
				variableValues.get(resultTable.get("VarName").toString()).add(resultTable.get("VarValue").toString());
			}
		} catch (Exception e) {
			logger.debug("No Variable results", e);
		}
		
		return variableValues;
	}
}
