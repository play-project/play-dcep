package eu.play_platform.platformservices.bdpl;
/**
 * Variable can be of different types. Depending on how they are used.
 * E.g. a variable can be used in event processing and in historic part.
 * The types are represented as a bitmap.
 * For this reason it is possible to combine different types and retrieve the types later.
 * 
 * @author sobermeier
 *
 */
public class VariableTypes {

	public static final int CONSTRUCT_TYPE = 1 << 0;
	public static final int REALTIME_TYPE  = 1 << 1;
	public static final int HISTORIC_TYPE  = 1 << 2;
	public static final int SIMPLE_TYPE = 1 << 3;
	public static final int COUNT_TYPE = 1 << 4;
	public static final int AVG_TYPE = 1 << 5;
	public static final int MIN_TYPE = 1 << 6;
	public static final int MAX_TYPE = 1 << 7;
	public static final int SUM_TYPE = 1 << 8;
}
