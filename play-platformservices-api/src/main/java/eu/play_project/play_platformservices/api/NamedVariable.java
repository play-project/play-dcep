package eu.play_project.play_platformservices.api;

import java.io.Serializable;

import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.DatatypeLiteral;
import org.ontoware.rdf2go.model.node.LanguageTagLiteral;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.UriOrVariable;
import org.ontoware.rdf2go.model.node.Variable;

/**
 * A placeholder in SPARQL expressions. The placeholders can be filled later,
 * e.g. when instantiating a SPARQL CONSTRUCT template when all values are
 * computed.
 * 
 * Variable names are specified using their plain name e.g., {@code "var1"}
 * instead of the SPARQL syntax <strike>{@code "?var1"} </strike> or <strike>
 * {@code "$var1"}</strike>.
 * 
 * @author Roland Stühmer
 * 
 * @see {@link Variable}
 */
public class NamedVariable implements ResourceOrVariable, UriOrVariable, NodeOrVariable, Serializable {

	private static final long serialVersionUID = 4764393638403440322L;
	
	private final String name;

	/**
	 * Provide a variable name using the plain name e.g., {@code "var1"}
	 * instead of the SPARQL syntax <strike>{@code "?var1"} </strike> or
	 * <strike> {@code "$var1"}</strike>.
	 * 
	 * @param name plain variable name valid for SPARQL
	 */
	public NamedVariable(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public Resource asResource() throws ClassCastException {
		throw new ClassCastException("A Variable cannot be seen as this");
	}
	
	public Literal asLiteral() throws ClassCastException {
		throw new ClassCastException("A Variable cannot be seen as this");
	}
	
	public DatatypeLiteral asDatatypeLiteral() throws ClassCastException {
		throw new ClassCastException("A Variable cannot be seen as this");
	}
	
	public LanguageTagLiteral asLanguageTagLiteral() throws ClassCastException {
		throw new ClassCastException("A Variable cannot be seen as this");
	}
	
	public URI asURI() throws ClassCastException {
		throw new ClassCastException("A Variable cannot be seen as this");
	}
	
	public BlankNode asBlankNode() throws ClassCastException {
		throw new ClassCastException("A Variable cannot be seen as this");
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	/**
	 * There is only one Variable, so it's equals to itself.
	 */
	@Override
	public boolean equals(Object other) {
		if(other instanceof NamedVariable) {
			return this.getName().equals(((NamedVariable) other).getName());
		}
		else {
			return false;
		}
	}
	
	public int compareTo(Node other) {
		if(other instanceof NamedVariable) {
			return this.getName().compareTo(((NamedVariable) other).getName());
		} else {
			return 1;
		}
	}
	
	public String toSPARQL() {
		return '$' + this.name;
	}
	
	@Override
	public String toString() {
		return this.toSPARQL();
	}
	
}
