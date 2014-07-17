/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.initiate.util;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;

/**
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public class ArrayAsignListener implements UpdateListener{
	
	private final BDPLArray array;
	
	public ArrayAsignListener(BDPLArray a){
		if(a == null){
			throw new IllegalArgumentException();
		}
		
		array = a;
	}
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		
		//TODO save new element into array 
	}

}
