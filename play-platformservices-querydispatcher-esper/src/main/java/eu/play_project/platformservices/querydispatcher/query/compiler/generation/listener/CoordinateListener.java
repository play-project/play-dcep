/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeResults;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.IBDPLFilter;
import eu.play_project.platformservices.querydispatcher.query.simulation.coordinateUI.CoordinatePanel;

/**
 * @author ningyuan 
 * 
 * Oct 9, 2014
 *
 */
public class CoordinateListener implements UpdateListener{
	
	private final RealTimeResults realTimeResults;
	
	private final List<IBDPLFilter<Map<String, String[]>>> arrayFilters;
	
	private CoordinatePanel panel;
	
	private final BDPLArrayTable varTable; 
	
	public CoordinateListener(RealTimeResults realTimeResults, List<IBDPLFilter<Map<String, String[]>>> arrayFilters, BDPLArrayTable vt){
		this.realTimeResults = realTimeResults;
		this.arrayFilters = arrayFilters;
		varTable = vt;
	}
	
	public void setPanel(CoordinatePanel p){
		panel = p;
	}
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		
		System.out.println(Thread.currentThread().getName()+"   CoordinateListener: ");
		
		for(int i = 0; i < newEvents.length; i++){
			//System.out.println("RealTimeResultListener result "+i+": ");
			
			List<Map<String, String[]>> result = realTimeResults.get();
			
			/*if(result != null){
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
			}*/
			
			if(panel != null){
				panel.repaint();
				
				String[][][] a = varTable.get("ecg").getArray().read();
				String[] l = new String[a.length];
				for(int j = 0; j < a.length; j++){
					l[j] = a[j][0][0];
				}
				panel.setPoints(l);
				
				panel.repaint();
			}
		}
		
	}
}
