/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;


import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;

/**
 * @author ningyuan
 *
 */
public class TestStmtListener implements UpdateListener{

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		/*for(int i = 0; i < newEvents.length; i++){
			RDFEvent e = (RDFEvent)newEvents[i].getUnderlying();
		}*/
		System.out.println("New Events[" + newEvents.length + "]");
		
		for(int i = 0; i < newEvents.length; i++){
			System.out.println("Result "+i+": ");
			EventBean eb = newEvents[i];
			EventType et = eb.getEventType();
			String[] enames =  et.getPropertyNames();
			
			for(String n : enames){
				System.out.print(n+":   ");
				MapEvent map = (MapEvent)eb.get(n);
				if(map != null){
					EventModel e = (EventModel)map.get(MapEvent.EVENT_MODEL);
					e.getProperties("http://ningyuan.com/id");
				}
				else{
					System.out.println();
				}
			}
			
		}
		
	}

}
