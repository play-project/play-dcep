/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia;

import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

/**
 * @author ningyuan 
 * 
 * Oct 1, 2014
 *
 */
public class EventSim implements Runnable{
	
	private long interval = 1000;
	private EventCreator eg;
	private EventOutput output;
	
	public EventSim(long i, String f, EPServiceProvider epS){
		interval = i;
		//eg = new ECGEventCreator(f);
		//eg = new ECGEventCreator1(f);
		eg = new MITECGEventCreator1(f);
		//eg = new RREventCreator(f);
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
			System.out.println("[ArrEvent Feeding is stoped]");
		}
	}

}
