/**
 * 
 */
package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
import eu.play_project.dcep.distributedetalis.api.EcConnectionmanagerException;
import eu.play_project.dcep.distributedetalis.api.HistoricalDataEngine;
import eu.play_project.dcep.distributedetalis.api.VariableBindings;
import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.HistoricalQuery;
import eu.play_project.play_platformservices.api.VariableNames;

/**
 * @author Ningyuan Pan
 * @author Roland Stühmer
 */
public class Engine implements HistoricalDataEngine {

	private final EcConnectionManager ecConnection;
	private final Logger logger;

	public Engine(EcConnectionManager ecm){
		if(ecm == null)
			throw new IllegalArgumentException("EcConnection must not be null");
		ecConnection = ecm;
		logger = LoggerFactory.getLogger(this.getClass());
	}


	@Override
	public HistoricalData get(
			List<HistoricalQuery> queries,
			VariableBindings variableBindings) {

		Map<String, String> historicalQueries = new HashMap<String, String>();
		Map<String, VariableNames> variableNames = new HashMap<String, VariableNames>();
		for (HistoricalQuery historicalQuery : queries) {
			historicalQueries.put(historicalQuery.getCloudId(), historicalQuery.getQuery());
			variableNames.put(historicalQuery.getCloudId(), historicalQuery.getVariables());
		}

		return this.get(historicalQueries, variableNames, variableBindings);
	}

	public HistoricalData get(Map<String, String> queries, Map<String, VariableNames> variableNames, VariableBindings variableBindings){
		HistoricalData ret = new HistoricalData();

		// data structures used by core
		List<SelectResults> rrs = new ArrayList<SelectResults>();
		Map<String, SelectVariable> svs = new HashMap<String, SelectVariable>();

		// init data structures used by core
		// separate the variables per stream:
		for(String stream : queries.keySet()){
			String query = queries.get(stream);
			VariableBindings vb = new VariableBindings();

			VariableNames vars = variableNames.get(stream);
			for(String var : vars){
				List<Object> binding = variableBindings.get(var);
				if (binding != null && !binding.isEmpty()) {
					vb.put(var, binding);
				}
			}
			HistoricalQueryContainer hq = new HistoricalQueryContainer(query, vb);

			if(!addResultRegistry(stream, hq.getQuery(), rrs, svs, variableNames)){
				return ret;
			}
		}

		Core.make(svs, rrs);
		transform(ret, rrs.get(0));
		return ret;
	}

	private boolean addResultRegistry(String stream, String hquery, List<SelectResults> rrs, Map<String, SelectVariable> svs, Map<String, VariableNames> variableNames) {

		SelectResults rr;
		try {
			rr = ecConnection.getDataFromCloud(hquery, stream);
		} catch (EcConnectionmanagerException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		if(rr.getSize() == 0)
			return false;
		else{
			VariableNames vars = variableNames.get(stream);
			for(String var : vars){
				SelectVariable sv = svs.get(var);
				if(sv == null){
					sv = new SelectVariable();
					svs.put(var, sv);
				}
				sv.addRelResult(rr);
			}
			rrs.add(rr);
			return true;
		}
	}

	private void transform(Map<String, List<String>> m, SelectResults rr){
		List<String> vars = rr.getVariables();
		List<List> result = rr.getResult();

		List<String>[] values = new ArrayList[vars.size()];
		for(int i = 0; i < vars.size(); i++){
			values[i] = new ArrayList<String>();
			m.put(vars.get(i), values[i]);
		}

		// every row in result
		for(int i = 0; i < result.size(); i++){
			List<String> r = result.get(i);
			// every column in one row
			for(int j = 0; j < r.size(); j++){
				values[j].add(r.get(j));
			}
		}
	}
}
