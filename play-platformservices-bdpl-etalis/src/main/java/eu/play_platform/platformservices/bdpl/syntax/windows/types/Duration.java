package eu.play_platform.platformservices.bdpl.syntax.windows.types;

import java.util.Calendar;

import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.xs.DurationDV;
import org.apache.xerces.xs.datatypes.XSDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/**
 * Represents a "xsd:duration element."
 * @author sobermeier
 *
 */
public class Duration extends Element {
	
	private Logger logger;
	private String timeInSeconds;
	
	public Duration(String value) {
		
		logger = LoggerFactory.getLogger(SlidingWindow.class);

		// Pars and store value.
		final String PREFIX = "(\"";
		final String POSTFIX = "\"^^xsd:duration";
		try {
			String tmp = value.substring(
					value.indexOf(PREFIX) + PREFIX.length(),
					value.lastIndexOf(POSTFIX));
			DurationDV dv = new DurationDV();
			XSDateTime dt = (XSDateTime) dv.getActualValue(tmp, null);
			long durationInMillis = dt.getDuration().getTimeInMillis(Calendar.getInstance());

			this.timeInSeconds = durationInMillis / 1000 + "";
		} catch (InvalidDatatypeValueException e) {
			logger.error("It is not possible to pars window values. This is a bug in the parser implementation. {}", e.getMessage());
			e.printStackTrace();
		}
		
	}
	public String getTimeInSeconds() {
		return this.timeInSeconds;
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
		return false;
	}

}
