package eu.play_platform.platformservices.bdpl.syntax.windows.types;

import java.util.Calendar;

import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.xs.DurationDV;
import org.apache.xerces.xs.datatypes.XSDateTime;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementDuration;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

import eu.play_platform.platformservices.bdpl.syntax.windows.Window;
import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
/**
 * Represents a parsed tumbling window expression.
 * @author sobermeier
 *
 */
public class TumblingWindow extends Window{
	
	public TumblingWindow(ElementDuration duration) {
		logger = LoggerFactory.getLogger(TumblingWindow.class);
		this.value = duration.getTimeInSeconds();
	}

	@Override
	public void accept(ElementWindowVisitor v) {
		v.visit(this);
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}
}
