package eu.play_project.play_platformservices.api;

import java.util.List;


/**
 * The QueryTemplate is used to represent quadruples with a mixture of fixed values and variables to be replaced later.
 * 
 * @author Stefan Obermeier
 * @author Roland Stühmer
 */
public interface QueryTemplate<QuadrupleType, QuadruplePatternType, UriType> {
	
	/**
	 * Add a new line to the template containing fixed RDF values or variables.
	 */
	public void appendLine(QuadruplePatternType templateLine);

	/**
	 * Instantiate the template by replacing all variables in the template with
	 * the values from the supplied map of "historical" data.
	 * 
	 * @param historicalData
	 *            Variable name followed by a list with values.
	 */
	public List<QuadrupleType> fillTemplate(HistoricalData historicalData, UriType graph, UriType eventId);
}
