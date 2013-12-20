package eu.play_project.querydispatcher.tests;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_PLACEHOLDER;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;

import eu.play_project.play_commons.constants.Namespace;
import eu.play_project.play_platformservices.QueryTemplateImpl;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class QueryTemplateImplTest {

	@Test
	public void testQueryTemplateImpl() {
		QueryTemplateImpl qt = new QueryTemplateImpl();
		qt.appendLine(NodeFactory.createURI("urn:1"), NodeFactory.createVariable("alice"), NodeFactory.createVariable("bob"), NodeFactory.createLiteral("100"));
		qt.appendLine(NodeFactory.createURI("urn:1"), NodeFactory.createURI(EVENT_ID_PLACEHOLDER), NodeFactory.createURI("urn:someuri"), NodeFactory.createLiteral("120"));
		qt.appendLine(NodeFactory.createURI("urn:2"), NodeFactory.createVariable("a"), NodeFactory.createVariable("b"), NodeFactory.createVariable("c"));
		
		HistoricalData hd = new HistoricalData();
		
		List<String> bindAlice = new LinkedList<String>();
		bindAlice.add("12345677");
		hd.put("alice", bindAlice);

		List<String> bindBob = new LinkedList<String>();
		bindBob.add("0000456");
		bindBob.add("0000457");
		hd.put("bob", bindBob);
		
		List<String> bindA = new LinkedList<String>();
		bindA.add("horse");
		bindA.add("cat");
		bindA.add("mouse");
		hd.put("a", bindA);
		
		List<String> bindB = new LinkedList<String>();
		bindB.add("car");
		bindB.add("truck");
		hd.put("b", bindB);
		
		List<String> bindC = new LinkedList<String>();
		bindC.add("table");
		bindC.add("chair");
		bindC.add("lamp");
		bindC.add("door");
		hd.put("c", bindC);
		
		List<Quadruple> result = qt.fillTemplate(hd, NodeFactory.createURI("urn:graphName"), NodeFactory.createURI("urn:event"));
		Assert.assertEquals("We expected 27 results.", 27, result.size());
		
		System.out.println(new CompoundEvent(result));
	}
	
	@Test
	public void generateTemplateForMissedCallsPlusTwitterQuery(){
		EleGeneratorForConstructQuery eleGenerator = new EleGeneratorForConstructQuery();
		
		// Get query.
		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
		
		// Parse query
		Query q = QueryFactory.create(queryString, Syntax.syntaxBDPL);

		// Generate CEP-language
		eleGenerator.setPatternId("'" + Namespace.PATTERN.getUri() + "123" + "'"); // TODO sobermeier: Remove in the future, ETALIS will do this
		eleGenerator.generateQuery(q);

		// Add queryDetails
		QueryDetails qd = this.createQueryDetails("'" + "123" + "'", q);

		BdplQuery bdpl = BdplQuery.builder()
				.details(qd)
				.bdpl(queryString)
				.ele(eleGenerator.getEle())
				.historicalQueries(PlaySerializer.serializeToMultipleSelectQueries(q))
				.constructTemplate(eleGenerator.getQueryTemplate())
				.build();

		System.out.println("FFF");
		
		
	}
	
	private QueryDetails createQueryDetails(String queryId, Query query) {

		QueryDetails qd = new QueryDetails();
		qd.setQueryId(queryId);

		StreamIdCollector streamIdCollector = new StreamIdCollector();
		streamIdCollector.getStreamIds(query, qd);

		return qd;
	}
	
	private String getSparqlQuery(String queryFile) {
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;

			while (null != (line = br.readLine())) {
				sb.append(line);
				sb.append("\n");
			}
			// System.out.println(sb.toString());
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
