package com.hp.hpl.jena.sparql.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;

import eu.play_project.play_platformservices_querydispatcher.historicalQuery.DetectCloudId;

public class HistoricalGraphFormaterElement extends FormatterElement {

	Map<String, String> historicalCloudQueries = new HashMap<String, String>();
	IndentedLineBuffer filterExp; // Represent filter in a named graph pattern.
	

	public HistoricalGraphFormaterElement(IndentedWriter out, SerializationContext context) {
		super(out, context);
	}

	@Override
	public void visit(ElementNamedGraph el) {

		// PLAY: Replace output buffer temporally to get one graph only
		IndentedWriter original = super.out;
		super.out = new IndentedLineBuffer();

		DetectCloudId.resetBuffer();
		
		// Process elements.
		filterExp = new IndentedLineBuffer();
		visitNodePattern("GRAPH", el.getGraphNameNode(), el.getElement());

		// Add filter after GRAPH{} 
		
		System.out.println("\n\n\n\n " + out.toString().substring(0, out.toString().length()));
		
		// Save cloud ID and the corresponding query.
		if (historicalCloudQueries.containsKey(DetectCloudId.getCloudId())) {
			historicalCloudQueries.put(DetectCloudId.getCloudId(),
			historicalCloudQueries.get(DetectCloudId.getCloudId()) + "\n" + out.toString() + "\n  " + filterExp);
		} else {
			historicalCloudQueries.put(DetectCloudId.getCloudId(), out.toString() + "\n  " + filterExp);
		}

		// Continue with old buffer.
		super.out = original;
	}
	
	@Override
	public void visit(ElementFilter el) {
		// PLAY: Replace output buffer temporally to get filter exp in own var.
		IndentedWriter original = super.out;
		super.out = filterExp;
		
		super.visit(el);
		
		// Continue with old buffer.
		super.out = original;
	}
	
	@Override
	public void visit(ElementPathBlock el) {

		// Use from serialization from parent class.
		super.visit(el);

		// Search cloud id.
		PathBlock pBlk = el.getPattern();
		for (TriplePath tp : pBlk) {
			DetectCloudId.newTriple(tp.getPredicate().toString(), tp.getObject().toString());
		}

	}
	

    @Override
    public void visit(ElementService el) {
		// PLAY: Replace output buffer temporally to get one graph only
		IndentedWriter original = super.out;

		super.out = new IndentedLineBuffer();
		
		// Set start state.
		DetectCloudId.resetBuffer();
		DetectCloudId.startServiceGraph();
		DetectCloudId.setCloudId(el.getServiceNode().toString());

        visitNodePattern("", null, el.getElement()) ;

		// Continue with old buffer.
		super.out = original;
    }

	public Map<String, String> getHistoricalCloudQueries() {
		return historicalCloudQueries;
	}

	// Generate string representation of prefixes.
	public String getPrefixNames() {
		String prefixes = "";
		// Get prefix names ans serialize them.
		for (String key : super.context.getPrefixMapping()
				.getNsPrefixMap().keySet()) {
			prefixes += "PREFIX "
					+ key
					+ ": <"
					+ super.context.getPrefixMapping().getNsPrefixMap()
							.get(key) + "> \n";
		}
		return prefixes;
	}
}
