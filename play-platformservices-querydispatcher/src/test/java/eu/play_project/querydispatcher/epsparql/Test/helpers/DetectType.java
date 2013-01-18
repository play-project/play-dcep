package eu.play_project.querydispatcher.epsparql.Test.helpers;

import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_LessThan;

public class DetectType {
	
	public String generateStringRepresentation(E_LessThan type){
		return "<";
	}
	
	public String generateStringRepresentation(E_GreaterThan type){
		return ">";
	}

}
