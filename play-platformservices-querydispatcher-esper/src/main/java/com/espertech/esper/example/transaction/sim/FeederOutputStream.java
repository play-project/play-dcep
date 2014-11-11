/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.example.transaction.sim;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.example.transaction.FindMissingEventStmt;

import eu.play_project.platformservices.querydispatcher.query.event.EventModel;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;


public class FeederOutputStream implements OutputStream
{
	
	private final EPServiceProvider epService;
    private final EPRuntime runtime;
    private final long startTimeMSec;

    // We keep increasing the current time to simulate a 30 minute window
    private long currentTimeMSec;
    
    private final Map<String, Object> mapDef = new HashMap<String, Object>();
    
    public FeederOutputStream(EPServiceProvider epS)
    {
    	this.epService = epS;
        this.runtime = epS.getEPRuntime();
        startTimeMSec = System.currentTimeMillis();
        currentTimeMSec = startTimeMSec;
        
        mapDef.put(MapEvent.EVENT_MODEL, EventModel.class);
    }

    @Override
	public void output(List<Object> bucket) throws IOException
    {
        log.info(".output Feeding " + bucket.size() + " events");

        long startTimeMSec = currentTimeMSec;
        long timePeriodLength = FindMissingEventStmt.TIME_WINDOW_TXNC_IN_SEC * 1000;
        long endTimeMSec = startTimeMSec + timePeriodLength;
        sendTimerEvent(startTimeMSec);

        int count = 0, total = 0;
        // output all element in bucket
        for (Object theEvent : bucket)
        {
            /*runtime.sendEvent(theEvent);*/
        	MapEventWrapper ew = (MapEventWrapper)theEvent;
        	String ename = ew.name;
        	if(ename.equals("rdf1")){
        		epService.getEPAdministrator().getConfiguration().addEventType("Event1", mapDef);
        		/*String eventType = ((Model)ew.event.get("model")).filter(null, RDF.TYPE, null).iterator().next().getObject().stringValue();*/
        		try {
        			runtime.sendEvent(ew.event, "Event1");
        				//System.out.println("Send Event1");
        		} catch (EPException e) {
        			log.info("Unknwon event type encountered: " );
        		}
        	}
        	else if(ename.equals("rdf2")){
        		epService.getEPAdministrator().getConfiguration().addEventType("Event2", mapDef);
        		runtime.sendEvent(ew.event, "Event2");
        			//System.out.println("Send Event2");
        	}
        	else{
        		epService.getEPAdministrator().getConfiguration().addEventType("Event3", mapDef);
        		runtime.sendEvent(ew.event, "Event3");
        			//System.out.println("Send Event3");
        	}
        	
            count++;
            total++;

            if (count % 1000 == 0)
            {
                sendTimerEvent(startTimeMSec + timePeriodLength * total / bucket.size());
                count = 0;
            }

            if (count == 10000)
            {
                log.info(".output Completed " + total + " events");
                count = 0;
            }
        }

        sendTimerEvent(endTimeMSec);
        currentTimeMSec = endTimeMSec;

        log.info(".output Completed bucket");
    }

    private void sendTimerEvent(long msec)
    {
        log.info(".sendTimerEvent Setting time to now + " + (msec - startTimeMSec));
    }

    private static final Log log = LogFactory.getLog(TxnGenMain.class);
}
