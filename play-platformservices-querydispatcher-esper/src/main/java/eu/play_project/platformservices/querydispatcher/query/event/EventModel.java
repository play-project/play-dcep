/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.event;

/**
 * The abstract class for the model of event itself. T is the actual implementation of
 * event model. This class also provides unified abstract methods for getting properties of an 
 * event.
 *   
 * 
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
	
	/**
	 * Test if an event has a property with indicated value.
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public abstract boolean isPropertyEquals(String property, String value);
	
	public abstract boolean isPropertyEquals(String property, String value, int level);
	
	/**
	 * Test if an event has a property.
	 * 
	 * @param property
	 * @return
	 */
	public abstract boolean hasProperty(String property);
	
	/**
	 * Get properties of an event.
	 * 
	 * @param property
	 * @return
	 */
	public abstract Object[] getProperties(String property);
	
}
