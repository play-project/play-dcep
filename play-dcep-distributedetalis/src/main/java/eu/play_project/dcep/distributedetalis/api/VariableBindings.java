package eu.play_project.dcep.distributedetalis.api;

import java.util.HashMap;
import java.util.List;

/**
 * Encapsulates value bindings which are incomplete. This functions as a restriction
 * for the final values returned by a database request for historical data. The bindings
 * must fulfill a join with the final data.
 * Variable names (the keys in this collection) are specified using the SPARQL "?",
 * e.g., {@code "?time"} instead of <strike>{@code "time"}</strike>.
 * 
 * @author Roland Stühmer
 */

public class VariableBindings extends HashMap<String, List<Object>> {
	private static final long serialVersionUID = -7694752146628934047L;

}
