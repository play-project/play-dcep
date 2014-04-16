/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.event;

/**
 * @author ningyuan 
 * 
 * Apr 16, 2014
 *
 */
public abstract class EventModel<T> {
	
	protected final T model;
	
	public EventModel(T model){
		this.model = model;
	}
	
	public T getModel(){
		return model;
	}
	
	public abstract boolean isPropertyEquals(String property, String value);
	
	public abstract boolean isPropertyEquals(String property, String value, int level);
	
	public abstract boolean hasProperty(String property);
	
	public abstract Object[] getProperties(String property);
	
}
