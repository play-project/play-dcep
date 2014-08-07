/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;


import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResults;
import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;

/**
 * @author ningyuan
 *
 */
public class RealTimeResultListener implements UpdateListener{
	
	private final RealTimeResults realTimeResults;
	
	public RealTimeResultListener(RealTimeResults realTimeResults){
		this.realTimeResults = realTimeResults;
	}
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		
		System.out.println(Thread.currentThread().getName()+"   RealTimeResultListener: ");
		
		for(int i = 0; i < newEvents.length; i++){
			System.out.println("Result "+i+": ");
			
			Map<String, String[]> result = realTimeResults.get();
			for(String key : result.keySet()){
				System.out.print(key+": "+result.get(key)[0]+"   "+result.get(key)[1]+"   ");
			}
				System.out.println();
			
			/*EventBean eb = newEvents[i];
			EventType et = eb.getEventType();
			String[] enames =  et.getPropertyNames();
			
			for(String n : enames){
				System.out.print(n+":   ");
				MapEvent<EventModel> map = (MapEvent<EventModel>)eb.get(n);
				if(map != null){
					EventModel e = (EventModel)map.get(MapEvent.EVENT_MODEL);
					e.getProperties("http://ningyuan.com/id");
				}
				else{
					System.out.println();
				}
			}*/
			
		}
		
	}

}
