/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;

import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResults;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLFilter;


/**
 * @author ningyuan
 *
 */
public class RealTimeResultListener implements UpdateListener{
	
	private final RealTimeResults realTimeResults;
	
	private final List<IBDPLFilter<Map<String, String[]>>> arrayFilters;
	
	public RealTimeResultListener(RealTimeResults realTimeResults, List<IBDPLFilter<Map<String, String[]>>> arrayFilters){
		this.realTimeResults = realTimeResults;
		this.arrayFilters = arrayFilters;
	}
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		
		System.out.println(Thread.currentThread().getName()+"   RealTimeResultListener: ");
		
		for(int i = 0; i < newEvents.length; i++){
			System.out.println("RealTimeResultListener result "+i+": ");
			
			List<Map<String, String[]>> result = realTimeResults.get();
			
			if(result != null){
				for(Map<String, String[]> varBinding : result){
						
						System.out.println("RealTimeResultListener var binding:");
						for(String key : varBinding.keySet()){
							System.out.print(key+": "+varBinding.get(key)[0]+"   "+varBinding.get(key)[1]+"   ");
						}
						System.out.println();
						
					for(IBDPLFilter<Map<String, String[]>> af : arrayFilters){
						af.setDataObject(varBinding);
						try {
							System.out.println("RealTimeResultListener array filter: "+af.evaluate());
						} catch (BDPLFilterException e) {
							e.printStackTrace();
						}
					}
				}	
			}
			
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
