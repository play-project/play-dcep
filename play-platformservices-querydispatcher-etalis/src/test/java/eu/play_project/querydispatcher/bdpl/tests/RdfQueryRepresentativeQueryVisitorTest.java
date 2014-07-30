package eu.play_project.querydispatcher.bdpl.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.RdfQueryRepresentativeQueryVisitor;

public class RdfQueryRepresentativeQueryVisitorTest {
	
	@Test
	public void testRdfQueryRepresentativeQueryVisitor() throws IOException {
	
		String queryString = BdplEleTest.getSparqlQuery("queries/HavingAvgExp2.eprq");
		Query query = null;
	
		try {
			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
		} catch (Exception e) {
			System.out.println("Exception was thrown: " + e);
		}
	
		RdfQueryRepresentativeQueryVisitor v = new RdfQueryRepresentativeQueryVisitor();
		query.getEventQuery().visit(v);
	
		// Queries for variable t1,e1,friend1,about1.
		String[] expectedResult = {
				"rdf(Ve1,'http://events.event-processing.org/types/temperature',Vt1,ViD0)",
				"rdf(Ve1,'http://www.w3.org/1999/02/22-rdf-syntax-ns#type','http://events.event-processing.org/types/FacebookStatusFeedEvent',ViD0)",
				"rdf(Ve1,'http://events.event-processing.org/types/name',Vfriend1,ViD0)",
				"rdf(Ve1,'http://events.event-processing.org/types/status',Vabout1,ViD0)"
		};
	
		int i = 0;
		for (String key : v.getRdfQueryRepresentativeQuery().keySet()) {
			assertEquals(expectedResult[i], v.getRdfQueryRepresentativeQuery().get(key));
			i++;
		}
	}

}
