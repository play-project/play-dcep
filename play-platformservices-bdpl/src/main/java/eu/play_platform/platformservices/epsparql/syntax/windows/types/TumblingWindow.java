package eu.play_platform.platformservices.epsparql.syntax.windows.types;

import java.util.Calendar;

import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.xs.DurationDV;
import org.apache.xerces.xs.datatypes.XSDateTime;
import org.slf4j.LoggerFactory;

import eu.play_platform.platformservices.epsparql.syntax.windows.Window;
import eu.play_platform.platformservices.epsparql.syntax.windows.visitor.ElementWindowVisitor;
/**
 * Represents a parsed tumbling window expression.
 * @author sobermeier
 *
 */
public class TumblingWindow extends Window{
	
	public TumblingWindow(String value) {
		
		logger = LoggerFactory.getLogger(TumblingWindow.class);
		
		// Pars and store value.
		final String PREFIX = "(\"";
		final String POSTFIX = "\"^^xsd:duration";
		try {
			String tmp = value.substring(
					value.indexOf(PREFIX) + PREFIX.length(),
					value.lastIndexOf(POSTFIX));
			DurationDV dv = new DurationDV();
			XSDateTime dt = (XSDateTime) dv.getActualValue(tmp, null);
			long durationInMillis = dt.getDuration().getTimeInMillis(
					Calendar.getInstance());

			this.value = durationInMillis / 1000 + "";
		} catch (InvalidDatatypeValueException e) {
			logger.error("It is not possible to pars window values. This is a bung in the parser implementation. " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void accept(ElementWindowVisitor v) {
		v.visit(this);
	}

}
