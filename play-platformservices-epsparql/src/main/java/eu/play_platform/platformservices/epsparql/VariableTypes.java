package eu.play_platform.platformservices.epsparql;
/**
 * A variable can appear in historic,  realtime or both parts of an query.
 * They have different types to identify them.
 * 
 * @author sobermeier
 *
 */
public enum VariableTypes {

	constructType,   // Variables from CONSTRUCT resultSet
	realtimeType, // Variables from part which is processed by CEP engine
	historicType,  // Variables form are filled by EventCloud
	preloadType
}
