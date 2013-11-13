package eu.play_project.dcep.distributedetalis.test;

import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.quoteForProlog;
import static eu.play_project.dcep.distributedetalis.utils.PrologHelpers.unquoteFromProlog;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PrologHelpersTest {

	@Test
	public void testWrappingAndUnwrappingStrings() {
		String number = "100.1";
		assertEquals(number, unquoteFromProlog(number));

		String variableName = "Vname";
		assertEquals(variableName, unquoteFromProlog(variableName));

		String message = "I am a long string";
		assertEquals(message, unquoteFromProlog(message));
		assertEquals(message, unquoteFromProlog(quoteForProlog(message)));

		String wrongQuotes = "'I am a wrongly quoted string";
		assertEquals(wrongQuotes, unquoteFromProlog(wrongQuotes));

		String quoted = "'" + message + "'";
		assertEquals(message, unquoteFromProlog(quoted));
		assertEquals(quoted, quoteForProlog(message));

	}
}
