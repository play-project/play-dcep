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
public class EventOutput {
	
	private final EPServiceProvider epService;
    private EPRuntime runtime;
    private Map<String, Object> mapDef = new HashMap<String, Object>();
    private final String eType;
    
    public EventOutput(EPServiceProvider epS, String et)
    {
    	this.epService = epS;
        this.runtime = epS.getEPRuntime();
        eType = et;
        mapDef.put(MapEvent.EVENT_MODEL, EventModel.class);
        epService.getEPAdministrator().getConfiguration().addEventType(eType, mapDef);
    }
    
    public void output(Map event){
    	
		try {
			
			runtime.sendEvent(event, eType);
				
		} catch (EPException e) {
			
		}
    }
}
