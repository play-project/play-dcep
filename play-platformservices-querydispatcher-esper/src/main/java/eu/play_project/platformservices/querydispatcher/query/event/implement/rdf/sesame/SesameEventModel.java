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
	
	private final Map<Integer, Boolean> match = new HashMap<Integer, Boolean>();
	
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
		
			System.out.print("RDFModel.getProperties("+property+"):");
		if(result.size() > 0){
			int i = 0;
			ret = new Object[result.size()];
			Iterator<Statement> it = result.iterator();
			while(it.hasNext()){
				ret[i] = it.next().getObject();
					System.out.print(" "+ret[i]);
				i++;
			}
		}
			System.out.println();
		
		return ret;
	}

	@Override
	public boolean isPropertyEquals(String property, String value, int level) {
			System.out.println("Thread: "+Thread.currentThread().getId()+ " Level: "+level);
		Model r = super.model.filter(null, new URIImpl("<http://ningyuan.com/>"), null);
		if(r.size() > 0){
			System.out.print("RDFModel.id: "+r.objectValue().toString()+"   ");
		}
		
		Model result = super.model.filter(null, new URIImpl(property), null);
		
		if(result.size() > 0){
			Iterator<Statement> it = result.iterator();
			
			while(it.hasNext()){
				Value v = it.next().getObject();
		
				if(v.toString().equals("\""+value+"\"")){
					Boolean m = match.get(level);
					
					if(m == null || !m.booleanValue()){
						System.out.println("RDFModel.isPropertyEquals("+property+", "+value+"): true [Mathched]");
						
						match.put(level, true);
						return true;
					}
					else{
						System.out.println("RDFModel.isPropertyEquals("+property+", "+value+"): true ");
						
						return false;
					}
				}
			}
		}
		
		System.out.println("RDFModel.isPropertyEquals("+property+", "+value+"): false");
		return false;
	}

	@Override
	public boolean isPropertyEquals(String property, String value) {
		Model r = super.model.filter(null, new URIImpl("<http://ningyuan.com/>"), null);
		if(r.size() > 0){
			System.out.print("RDFModel.id: "+r.objectValue().toString()+"   ");
		}
		
		Model result = super.model.filter(null, new URIImpl(property), null);
		
		if(result.size() > 0){
			Iterator<Statement> it = result.iterator();
			
			while(it.hasNext()){
				Value v = it.next().getObject();
		
				if(v.toString().equals("\""+value+"\"")){
					
						System.out.println("RDFModel.isPropertyEquals("+property+", "+value+"): true ");
						
						return true;
					
				}
			}
		}
		
			System.out.println("RDFModel.isPropertyEquals("+property+", "+value+"): false");
		return false;
	}
}
