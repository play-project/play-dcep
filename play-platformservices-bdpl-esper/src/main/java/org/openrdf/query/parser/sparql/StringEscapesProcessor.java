package org.openrdf.query.parser.sparql;

import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;
import org.openrdf.query.parser.sparql.ast.ASTString;
import org.openrdf.query.parser.sparql.ast.VisitorException;

import org.openrdf.query.MalformedQueryException;


/**
 * Processes escape sequences in strings, replacing the escape sequence with
 * their actual value. Escape sequences for SPARQL are documented in section <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#grammarEscapes">A.7 Escape
 * sequences in strings</a>.
 * 
 * @author Arjohn Kampman
 */
public class StringEscapesProcessor {
	/**
	 * Processes escape sequences in ASTString objects.
	 * 
	 * @param qc
	 *        The query that needs to be processed.
	 * @throws MalformedQueryException
	 *         If an invalid escape sequence was found.
	 */
	public static void process(ASTOperationContainer qc)
		throws MalformedQueryException
	{
		StringProcessor visitor = new StringProcessor();
		try {
			qc.jjtAccept(visitor, null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	private static class StringProcessor extends eu.play_project.platformservices.bdpl.parser.ASTVisitorBase {

		public StringProcessor() {
		}

		@Override
		public Object visit(ASTString stringNode, Object data)
			throws VisitorException
		{
			String value = stringNode.getValue();
			try {
				value =  BDPLUtil.decodeString(value);
				stringNode.setValue(value);
			}
			catch (IllegalArgumentException e) {
				// Invalid escape sequence
				throw new VisitorException(e.getMessage());
			}
			
			// visit children nodes
			return super.visit(stringNode, data);
		}
	}
}
