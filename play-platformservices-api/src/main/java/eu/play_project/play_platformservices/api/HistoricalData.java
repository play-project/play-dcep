package eu.play_project.play_platformservices.api;

import java.util.HashMap;
import java.util.List;

/**
 * Encapsulates historical values for certain variables which were requested.
 * 
 * Variable names (the keys in this collection) are specified using their plain
 * name e.g., {@code "var1"} instead of the SPARQL syntax <strike>{@code "?var1"}
 * </strike> or <strike>{@code "$var1"}</strike>.
 * 
 * @author Roland Stühmer
 * 
 * @see {@link VariableNames}
 */
public class HistoricalData extends HashMap<String, List<String>> {

	private static final long serialVersionUID = 100L;

}
