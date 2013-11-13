package eu.play_project.dcep.distributedetalis.api;

import java.util.HashMap;
import java.util.List;

import eu.play_project.play_platformservices.api.HistoricalData;
import eu.play_project.play_platformservices.api.VariableNames;

/**
 * Encapsulates value bindings which are incomplete. This functions as a
 * restriction for further queries by a database request for historical data. In
 * the end these bindings serve to fulfill a join with the final data.
 * 
 * Variable names (the keys in this collection) are specified using their plain
 * name e.g., {@code "var1"} instead of the SPARQL syntax <strike>{@code "?var1"}
 * </strike> or <strike>{@code "$var1"}</strike>.
 * 
 * @author Roland Stühmer
 * 
 * @see {@link VariableNames}
 * @see {@link HistoricalData}
 */
public class VariableBindings extends HashMap<String, List<Object>> {
	private static final long serialVersionUID = 100L;

}
