/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.platformservices.querydispatcher.query.event.EventModel;

/**
 * An implementation of EventModel. Sesame event model is chosen to be the 
 * actual event model. 
 * 
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public class SesameEventModel extends EventModel<Model> {
	
	private final Logger logger = LoggerFactory.getLogger(SesameEventModel.class);
	
	private Map<Integer, Boolean> match = new HashMap<Integer, Boolean>();
	
	public SesameEventModel(Model model) {
		super(model);
	}
	
	@Override
	public boolean hasProperty(String property) {
		return super.model.contains(null, new URIImpl(property), null);
	}
	
	@Override
	public Object[] getProperties(String property) {
		Object [] ret = null;
	
		Model result = super.model.filter(null, new URIImpl(property), null);
		
		logger.debug("RDFModel.getProperties("+property+"):");
		if(result.size() > 0){
			int i = 0;
			ret = new Object[result.size()];
			Iterator<Statement> it = result.iterator();
			while(it.hasNext()){
				ret[i] = it.next().getObject();
					logger.debug(" "+ret[i]);
				i++;
			}
		}
		logger.debug("\n");
		
		return ret;
	}

	@Override
	public boolean isPropertyEquals(String property, String value, int level) {
		logger.debug("level: "+level);
		Model r = super.model.filter(null, new URIImpl("<http://ningyuan.com/>"), null);
		if(r.size() > 0){
			logger.debug("RDFModel.id: "+r.objectValue().toString()+"   ");
		}
		
		Model result = super.model.filter(null, new URIImpl(property), null);
		
		if(result.size() > 0){
			Iterator<Statement> it = result.iterator();
			
			while(it.hasNext()){
				Value v = it.next().getObject();
		
				if(v.toString().equals("\""+value+"\"")){
					Boolean m = match.get(level);
					
					if(m == null || !m.booleanValue()){
						logger.debug("RDFModel.isPropertyEquals("+property+", "+value+"): true [Mathched]");
						
						match.put(level, true);
						return true;
					}
					else{
						logger.debug("RDFModel.isPropertyEquals("+property+", "+value+"): true ");
						
						return false;
					}
				}
			}
		}
		
		logger.debug("RDFModel.isPropertyEquals("+property+", "+value+"): false");
		return false;
	}

	@Override
	public boolean isPropertyEquals(String property, String value) {
		Model r = super.model.filter(null, new URIImpl("<http://ningyuan.com/>"), null);
		if(r.size() > 0){
			logger.debug("RDFModel.id: "+r.objectValue().toString()+"   ");
		}
		
		Model result = super.model.filter(null, new URIImpl(property), null);
		
		if(result.size() > 0){
			Iterator<Statement> it = result.iterator();
			
			while(it.hasNext()){
				Value v = it.next().getObject();
		
				if(v.toString().equals("\""+value+"\"")){
					
						logger.debug("RDFModel.isPropertyEquals("+property+", "+value+"): true ");
						
						return true;
					
				}
			}
		}
		
			logger.debug("RDFModel.isPropertyEquals("+property+", "+value+"): false");
		return false;
	}
}
