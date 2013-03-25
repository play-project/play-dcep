package eu.play_project.dcep;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerLocal;
import eu.play_project.dcep.distributedetalis.api.VariableBindings;
import eu.play_project.dcep.distributedetalis.join.Engine;
import eu.play_project.play_platformservices.api.EpSparqlQuery;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.historic.QueryTemplateGenerator;

public class HistoricDataTest {

	@Test
	public void testHistoricData() throws IOException {
		
		Engine historicData = new Engine(new EcConnectionManagerLocal("historical-data/play-bdpl-personalmonitoring-historical-data.trig"));
		
		String queryFile = "play-bdpl-personalmonitoring.eprq";
		String query = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(queryFile), "UTF-8");
		String queryId = queryFile;
		
		Query q;
		q = QueryFactory.create(query, Syntax.syntaxEPSPARQL_20);


		// Add queryDetails
		QueryDetails qd = new QueryDetails(queryId);
		EpSparqlQuery epQuery = new EpSparqlQuery(qd, "");
		
		//Generate historical query.
		epQuery.setHistoricalQueries(PlaySerializer.serializeToMultipleSelectQueries(q));
		epQuery.setConstructTemplate((new QueryTemplateGenerator()).createQueryTemplate(q));
		
		// Add EP-SPARQL query.
		epQuery.setEpSparqlQuery(query);
		
		VariableBindings variableBindings = new VariableBindings();
		variableBindings.put("?tweetContent", Arrays.asList(new Object[] {"Tweettext 2", "bogus"}));
		variableBindings.put("?id2", Arrays.asList(new Object[] {Node.createURI("http://events.event-processing.org/ids/e5917518587088559184")}));

		//Get historical data to the given binding.
		HistoricalData values = historicData.get(epQuery.getHistoricalQueries(), variableBindings);
		
		for (String varName : values.keySet()) {
			System.out.format("Bindings for %s: %s\n", varName, values.get(varName));
		}

	}
}
