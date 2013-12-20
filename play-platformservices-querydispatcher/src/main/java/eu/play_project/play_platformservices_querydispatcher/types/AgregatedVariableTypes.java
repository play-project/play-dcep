package eu.play_project.play_platformservices_querydispatcher.types;
//package eu.play_project.play_platformservices_querydispatcher;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.hp.hpl.jena.query.Query;
//
//import eu.play_project.play_platformservices_querydispatcher.types.C_Quadruple;
//import eu.play_project.play_platformservices_querydispatcher.types.H_Quadruple;
//import eu.play_project.play_platformservices_querydispatcher.types.R_Quadruple;
//import fr.inria.eventcloud.api.Quadruple;
//
//public class AgregatedVariableTypes {
//
//	public enum AgregatedEventType {
//		C, H, R, CR, CH, RH, CRH
//	}
//
//	Logger logger = LoggerFactory.getLogger(AgregatedVariableTypes.class);
//
//	public Map<String, AgregatedEventType> detectType(Query query) {
//		
//		//Search variables.
//		VariableQuadrupleVisitor vqv = new VariableQuadrupleVisitor();
//		Map<String, List<Quadruple>> variables = null;// = vqv.getVariables(query);
//
//		// Result map.
//		Map<String, AgregatedEventType> variableAbsolutType = new HashMap<String, AgregatedEventType>();
//
//		// Print all variables and triples in which they occur.
//		for (String key : variables.keySet()) {
//			logger.debug("Variable " + key + " occurs in: ");
//			int type = 0;
//			boolean cSet = false;
//			boolean rSet = false;
//			boolean hSet = false;
//			for (Quadruple quadruple : variables.get(key)) {
//				logger.debug("Type is: " + quadruple.getClass().getName());
//
//				if (quadruple instanceof C_Quadruple) {
//					if(!cSet) type += 1; cSet= true;
//				}
//				if (quadruple instanceof R_Quadruple) {
//					if(!rSet)type += 2; rSet = true;
//				}
//				if (quadruple instanceof H_Quadruple) {
//					if(!hSet) type += 4; hSet = true;
//				}
//			}
//			
//			// Add type
//			switch (type) {
//			case 1:
//				variableAbsolutType.put(key, AgregatedEventType.C);
//				break;
//			case 2:
//				variableAbsolutType.put(key, AgregatedEventType.R);
//				break;
//
//			case 3:
//				variableAbsolutType.put(key, AgregatedEventType.CR);
//				break;
//
//			case 4:
//				variableAbsolutType.put(key, AgregatedEventType.H);
//				break;
//
//			case 5:
//				variableAbsolutType.put(key, AgregatedEventType.CH);
//				break;
//			case 6:
//				variableAbsolutType.put(key, AgregatedEventType.RH);
//				break;
//
//			case 7:
//				variableAbsolutType.put(key, AgregatedEventType.CRH);
//				break;
//				
//			case 8:
//				variableAbsolutType.put(key, AgregatedEventType.H);
//				break;
//
//			case 9:
//				variableAbsolutType.put(key, AgregatedEventType.CH);
//				break;
//				
//			case 10:
//				variableAbsolutType.put(key, AgregatedEventType.RH);
//				break;
//
//			case 11:
//				variableAbsolutType.put(key, AgregatedEventType.CRH);
//				break;
//				
//			case 12:
//				variableAbsolutType.put(key, AgregatedEventType.H);
//				break;
//				
//			}
//		}
//		return variableAbsolutType;
//	}
//
//}
