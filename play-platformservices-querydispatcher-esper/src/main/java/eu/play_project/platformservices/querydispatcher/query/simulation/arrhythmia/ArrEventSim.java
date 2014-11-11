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
public class ArrEventSim implements Runnable{
	
	private long interval = 1000;
	private ArrEventGreator eg;
	private ArrEventOutput output;
	
	public ArrEventSim(long i, String f, EPServiceProvider epS){
		interval = i;
		eg = new ArrEventGreator(f);
		output = new ArrEventOutput(epS);
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
