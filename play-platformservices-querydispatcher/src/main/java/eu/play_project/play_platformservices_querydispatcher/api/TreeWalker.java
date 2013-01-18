package eu.play_project.play_platformservices_querydispatcher.api;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;

public interface TreeWalker {
	
	public void walk(Element el, ElementVisitor visitor);

}
