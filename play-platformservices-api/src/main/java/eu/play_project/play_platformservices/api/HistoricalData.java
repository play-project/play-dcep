package eu.play_project.play_platformservices.api;

import java.util.HashMap;
import java.util.List;

/**
 * Encapsulates historical values for certain variables which were requested.
 * Variable names (the keys in this collection) are specified without the SPARQL "?",
 * e.g., {@code "time"} instead of <strike>{@code "?time"}</strike>.
 * 
 * @author Roland Stühmer
 */
public class HistoricalData extends HashMap<String, List<String>> {

	private static final long serialVersionUID = 100L;

}
