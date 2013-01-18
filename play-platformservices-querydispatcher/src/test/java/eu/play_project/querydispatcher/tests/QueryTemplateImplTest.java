package eu.play_project.querydispatcher.tests;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_PLACEHOLDER;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;

import eu.play_project.play_platformservices.QueryTemplateImpl;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

public class QueryTemplateImplTest {

	@Test
	public void testQueryTemplateImpl() {
		QueryTemplateImpl qt = new QueryTemplateImpl();
		qt.appendLine(Node.createURI("urn:1"), Node.createVariable("alice"), Node.createVariable("bob"), Node.createLiteral("100"));
		qt.appendLine(Node.createURI("urn:1"), Node.createURI(EVENT_ID_PLACEHOLDER), Node.createURI("urn:someuri"), Node.createLiteral("120"));
		qt.appendLine(Node.createURI("urn:2"), Node.createVariable("a"), Node.createVariable("b"), Node.createVariable("c"));
		
		Map<String, List<String>> vb = new HashMap<String, List<String>>();
		
		List<String> bindAlice = new LinkedList<String>();
		bindAlice.add("12345677");
		vb.put("alice", bindAlice);

		List<String> bindBob = new LinkedList<String>();
		bindBob.add("0000456");
		bindBob.add("0000457");
		vb.put("bob", bindBob);
		
		List<String> bindA = new LinkedList<String>();
		bindA.add("horse");
		bindA.add("cat");
		bindA.add("mouse");
		vb.put("a", bindA);
		
		List<String> bindB = new LinkedList<String>();
		bindB.add("car");
		bindB.add("truck");
		vb.put("b", bindB);
		
		List<String> bindC = new LinkedList<String>();
		bindC.add("table");
		bindC.add("chair");
		bindC.add("lamp");
		bindC.add("door");
		vb.put("c", bindC);
		
		List<Quadruple> result = qt.fillTemplate(vb, Node.createURI("urn:graphName"), Node.createURI("urn:event"));
		Assert.assertEquals("We expected 27 results.", 27, result.size());
		
		System.out.println(new CompoundEvent(result));
	}
}
