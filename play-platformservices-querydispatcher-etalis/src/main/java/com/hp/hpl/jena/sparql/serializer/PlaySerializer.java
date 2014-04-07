package com.hp.hpl.jena.sparql.serializer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedLineBuffer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode;

import eu.play_platform.platformservices.bdpl.VariableTypes;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;


public class PlaySerializer extends Serializer{
	
	/**
	 * Generate historical queries dependent on the destination cloud Id.
	 * 
	 * @return Key: CloudID, Value: String representation of historical select
	 *         query.
	 */
    public static List<HistoricalQuery> serializeToMultipleSelectQueries(Query query){
        // For the query pattern
        SerializationContext cxt1 = new SerializationContext(query, new NodeToLabelMapBNode("b", false) ) ;
        
        // For the construct pattern
        SerializationContext cxt2 = new SerializationContext(query, new NodeToLabelMapBNode("c", false)  ) ;
        IndentedLineBuffer writer = new IndentedLineBuffer() ;
        
        HistoricalGraphFormaterElement e = new HistoricalGraphFormaterElement(writer, cxt1);
        
        serializeARQ(query, writer,
                     e,
                     new FmtExprSPARQL(writer, cxt1),
                     new FmtTemplate(writer, cxt2));

        List<HistoricalQuery> result = new LinkedList<HistoricalQuery>();
        HistoricalQuery hq;
        
		Map<String, String> queries = e.getHistoricalCloudQueries();

		// Find shared variables for select query
		VariableTypeManager vtm = new VariableTypeManager(query);
		vtm.collectVars();
		Set<String> vars = vtm.getSelectSharedVariables(VariableTypes.REALTIME_TYPE, VariableTypes.CONSTRUCT_TYPE, VariableTypes.HISTORIC_TYPE);

		String selectString = null;

		// Add select variables
		for (String key : queries.keySet()) {
			hq = new HistoricalQuery();
			// Add prefix
			selectString = e.getPrefixNames() + "\n";
			selectString += "SELECT DISTINCT ";
			
			// Variables in current query.
			for (String var : vars) {
				selectString += " ?" + var;
				//Set variables.
				hq.getVariables().add(var);
			}
			
			if(vars.size() == 0) {
				selectString += "*";
			}
			
			// Set values.
			hq.setQuery(selectString +" \n WHERE { \n  " + queries.get(key) + "\n } ");
			hq.setCloudId(key);
			
			VariableTypeManager vm = new VariableTypeManager(query);
			vm.collectVars();
			if(vm.getIntersection(VariableTypes.HISTORIC_TYPE, VariableTypes.REALTIME_TYPE).size() > 0) {
				hq.setHasSharedVariablesWithRealtimePart(true);
			}
			
			result.add(hq);
		}
		

		return result;
	}
}
