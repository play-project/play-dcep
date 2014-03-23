package eu.play_project.play_platformservices.api;

import java.util.Collection;
import java.util.HashSet;

/**
 * Encapsulates a set of variable names used for building a generated historic
 * query.
 * 
 * Variable names (the keys in this collection) are specified using their plain
 * name e.g., {@code "var1"} instead of the SPARQL syntax <strike>{@code "?var1"}
 * </strike> or <strike>{@code "$var1"}</strike>.
 * 
 * @author Roland Stühmer
 * 
 * @see {@link HistoricalData}
 */
public class VariableNames extends HashSet<String> {
	
	/**
	 * @see {@link HashSet#HashSet(Collection)
	 */
	public VariableNames(Collection<? extends String> c) {
		super(c);
	}

	public VariableNames() {
		super();
	}
	private static final long serialVersionUID = 100L;

}
