/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;

import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;

/**
 * @author ningyuan 
 * 
 * Oct 1, 2014
 *
 */
public class ArrEventOutput {
	
	private final EPServiceProvider epService;
    private EPRuntime runtime;
    private Map<String, Object> mapDef = new HashMap<String, Object>();
    
    public ArrEventOutput(EPServiceProvider epS)
    {
    	this.epService = epS;
        this.runtime = epS.getEPRuntime();
        mapDef.put(MapEvent.EVENT_MODEL, EventModel.class);
    }
    
    public void output(Map event){
    	epService.getEPAdministrator().getConfiguration().addEventType("ArrEvent", mapDef);
		
		try {
			
			runtime.sendEvent(event, "ArrEvent");
				
		} catch (EPException e) {
			
		}
    }
}
