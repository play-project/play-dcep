package eu.play_project.dcep.distributedetalis.utils;

/**
 * A few helper methods to deal with data from Prolog.
 * 
 * @author Roland Stühmer
 */
public class PrologHelpers {

	/**
	 * Surround a String in single quotes to escape illegal charaters in Prolog
	 * or accidental variable names.
	 */
	public static String quoteForProlog(String s) {
		return '\'' + s + '\'';
	}
	
	/**
	 * Unwrap a String value from Prolog if it was quoted. Otherwise return
	 * String unchanged.
	 */
	public static String unquoteFromProlog(String s) {
		if (s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'') {
			return s.substring(1, s.length() - 1);
		} else {
			return s;
		}
	}

	/**
	 * Escape all characters which are illegal in Prolog's quoted strings:
	 * {@code It's me, Mario.} becomes {@code It\'s me, Mario.}. The resulting
	 * strings are meanto to be used as <i>quoted atoms</i> in Prolog, between
	 * single quotes.
	 * 
	 * @see <a
	 *      href="http://www.swi-prolog.org/pldoc/doc_for?object=section%284,%272.15.1.2%27,swi%28%27/doc/Manual/syntax.html%27%29%29">SWI
	 *      Prolog Character Escape Syntax</a>
	 */
	public static String escapeForProlog(String s) {
		return s.replaceAll("'", "\'");
	}
}
