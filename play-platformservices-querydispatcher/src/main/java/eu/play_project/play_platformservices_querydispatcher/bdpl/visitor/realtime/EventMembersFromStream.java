package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_platformservices.api.QueryDetails;


/**
 * Collect variables/values for member feature.
 * Use subject of the stream. In real time part only.
 * 
 * @author sobermeier
 *
 */
public class EventMembersFromStream {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


		/**
		 * Returns the input stream IDs without the {@code #stream} suffix to be used with EC and DSB.
		 * 
		 * @param query
		 * @return
		 */
		public Set<Node> getMembersRepresentative(Query query) {		
			ValueOrganizerVisitor valueOrganizerVisitor  = new ValueOrganizerVisitor();
			return valueOrganizerVisitor.getMembersRepresentative();
		}
		

		// Return value of URI element and traverse form ElementPathBlock to URI
		// element.
		private class ValueOrganizerVisitor extends GenericVisitor {
			Set<Node> membersRepresentative;

			public ValueOrganizerVisitor(){
				membersRepresentative =  new HashSet<Node>();
			}
			
			@Override
			public void visit(ElementEventBinOperator el) {
				el.getLeft().visit(this);
				el.getRight().visit(this);
			}
			
			@Override
			public void visit(ElementNamedGraph el){
				el.getElement().visit(this);
			}
			
			@Override
			public void visit(ElementEventGraph el) {
				el.getElement().visit(this);
			}
			
			@Override
			public void visit(ElementGroup el) {
				// Visit all group elements
				for (Element element : el.getElements()) {
					element.visit(this);
				}
			}
			
			@Override
			public void visit(ElementPathBlock el) {
				TypeCheckVisitor v = new TypeCheckVisitor();
				for (TriplePath tmpTriplePath : el.getPattern().getList()) {
					// Check if type is ok
					if (tmpTriplePath.getPredicate().visitWith(v) != null) {
						membersRepresentative.add(tmpTriplePath.getSubject());
							break;
						}
					}
				}

			public Set<Node> getMembersRepresentative() {
				return this.membersRepresentative;
			}

		}

		// Test if the type is http://events.event-processing.org/types/stream
		class TypeCheckVisitor extends GenericVisitor {
			private final String ok = "OK";

			@Override
			public Object visitURI(Node_URI it, String uri) {

				if (uri.equals(org.event_processing.events.types.Event.STREAM.toString()) || uri.equals((org.event_processing.events.types.Event.STREAM + "/"))) {
					return ok;
				}
				return null;
			}
		}
}

		
		
		
	



