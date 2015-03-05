/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;

import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;


/**
 * @author ningyuan 
 * 
 * Oct 1, 2014
 *
 */
public class EventFeeder implements Runnable{
	
	private long interval = 1000;
	private EventCreator eg;
	private EventOutput output;
	
	
	public EventFeeder(long i, String t, String f, EPServiceProvider epS){
		interval = i;
		
		eg = EventCreatorFactory.getEventCreator(t);
		if(eg == null || epS == null){
			throw new IllegalArgumentException();
		}
		
		eg.initiate(f);
		output = new EventOutput(epS, eg.getEventType());
	}
	
	@Override
	public void run() {
		try{
			while(true){
				Thread.sleep(interval);
				Map event = eg.next();
				if(event == null){
					break;
				}
				else{
					output.output(event);
				}
			}
		}catch(InterruptedException ex){
			
		}finally{
			eg.close();
			System.out.println("[Event Feeder is stoped]");
		}
	}

}
