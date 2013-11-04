package eu.play_project.dcep;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerLocal;
import eu.play_project.dcep.distributedetalis.api.VariableBindings;
import eu.play_project.dcep.distributedetalis.join.Engine;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;

public class HistoricDataTest {

	@Test
	public void testHistoricData() throws IOException {
		
		final String TEST_URI = "http://events.event-processing.org/ids/e5917518587088559184";
		
		
		Engine historicData;
		VariableBindings variableBindings;
		HistoricalData values;
		
		String queryFile = "patterns/play-bdpl-personalmonitoring.eprq";
		String query = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(queryFile), "UTF-8");
		String queryId = queryFile;
		
		Query q;
		q = QueryFactory.create(query, Syntax.syntaxBDPL);


		// Add queryDetails
		QueryDetails qd = new QueryDetails(queryId);
		BdplQuery bdpl = BdplQuery.builder()
				.details(qd)
				.bdpl(query)
				.ele("")
				.historicalQueries(PlaySerializer.serializeToMultipleSelectQueries(q))
				.constructTemplate(new QueryTemplateGenerator().createQueryTemplate(q))
				.build();
				
		variableBindings = new VariableBindings();
		variableBindings.put("?tweetContent", Arrays.asList(new Object[] {"Tweettext 2", "bogus"}));
		variableBindings.put("?id2", Arrays.asList(new Object[] {NodeFactory.createURI(TEST_URI)}));

		//Get historical data to the given binding.
		historicData = new Engine(new EcConnectionManagerLocal("historical-data/play-bdpl-personalmonitoring-historical-data.trig"));
		values = historicData.get(bdpl.getHistoricalQueries(), variableBindings);
		
		for (String varName : values.keySet()) {
			System.out.format("Bindings for %s: %s\n", varName, values.get(varName));
		}
		
		Assert.assertTrue("A result including the specified binding was expected.", values.get("id2").contains(TEST_URI));

		
		/*
		 * Do a second run, this time with an unmatchable binding (expecting empty results):
		 */
		variableBindings = new VariableBindings();
		variableBindings.put("?tweetContent", Arrays.asList(new Object[] {"Tweettext 2", "bogus"}));
		variableBindings.put("?id2", Arrays.asList(new Object[] {NodeFactory.createURI(TEST_URI + "error")}));

		//Get historical data to the given binding.
		historicData = new Engine(new EcConnectionManagerLocal("historical-data/play-bdpl-personalmonitoring-historical-data.trig"));
		values = historicData.get(bdpl.getHistoricalQueries(), variableBindings);
		
		for (String varName : values.keySet()) {
			System.out.format("Bindings for %s: %s\n", varName, values.get(varName));
		}
		
		Assert.assertTrue("An empty result was expected for the specified bindings.", values.isEmpty());

	}
}
