/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.initiate.util;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

/**
 * @author ningyuan 
 * 
 * Jul 3, 2014
 *
 */
public class SubQueryTableEntry {
	
	private EPStatement query;
	
	private UpdateListener listener;
}
