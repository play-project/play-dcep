/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.construct.ConstructTemplateFiller;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeSolutionSequence;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeSolutionSequence.RealTimeSolution;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.ConstructTemplate;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;
import eu.play_project.platformservices.querydispatcher.query.simulation.coordinateUI.CoordinatePanel;

/**
 * The listener class of the EPL statement. It receives the real-time data from
 * a bdpl query, tries to draw data in array "coordinate" on a coordinate system,
 * tries to join the real-time data with historic data and creates
 * response event using a construct template.
 * 
 * 
 * 
 * @author ningyuan 
 * 
 * Oct 9, 2014
 *
 */
public class CoordinateSystemListener implements UpdateListener{
	
	private EPServiceProvider epService;
    
	private EPRuntime runtime;
	
	private final RealTimeSolutionSequence realTimeResults;
	
	private final List<BDPLArrayFilter> arrayFilters;
	
	private CoordinatePanel panel;
	
	private String eventType;
	
	private ConstructTemplate constructTemplate;
	
	/*
	 * mainly for static arrays
	 */
	private BDPLArrayTable arrayTable;
	
	public CoordinateSystemListener(RealTimeSolutionSequence realTimeResults, List<BDPLArrayFilter> arrayFilters, ConstructTemplate constructTemplate, BDPLArrayTable vt){
		
		this.realTimeResults = realTimeResults;
		this.constructTemplate = constructTemplate;
		this.arrayFilters = arrayFilters;
		arrayTable = vt;
	}
	
	// must be called before deploy
	public void setEPServiceProvider(EPServiceProvider epS){
		epService = epS;
        runtime = epS.getEPRuntime();
        
        if(constructTemplate != null && constructTemplate.getRdfType() != null){
			Map<String, Object> mapDef = new HashMap<String, Object>();
			eventType = constructTemplate.getRdfType().replaceAll("[^a-zA-Z0-9]", "");
				
			epService.getEPAdministrator().getConfiguration().addEventType(eventType, mapDef);
		}
	}
	
	public void setPanel(CoordinatePanel p){
		panel = p;
	}
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		
		//System.out.println(Thread.currentThread().getName()+"   CoordinateListener: ");
		
		for(int i = 0; i < newEvents.length; i++){
			
			if(panel != null){
				panel.repaint();
				//XXX array var name
				BDPLArrayTableEntry bate = arrayTable.get("coordinate");
				if(bate != null){
					String[][][] a = bate.getArray().read();
					String[] l = new String[a.length];
					for(int j = 0; j < a.length; j++){
						l[j] = a[j][0][1];
					}
					
					panel.setPoints(l);
					
					panel.repaint();
				}
			}
			
			RealTimeSolution result = realTimeResults.get();
			
			if(result != null){
				for(Map<String, String[]> varBinding : result.getVarBindings()){
						
						/*System.out.println("CoordinateListener var binding:");
						for(String key : varBinding.keySet()){
							System.out.print(key+": "+varBinding.get(key)[0]+"   "+varBinding.get(key)[1]+"   ");
						}
						System.out.println();*/
						
					for(BDPLArrayFilter af : arrayFilters){
						af.setDataObject(varBinding, result.getDynamicArrays());
						try {
							if(!af.evaluate()){
									return;
							}
						} catch (BDPLFilterException e) {
							e.printStackTrace();
						}
					}
				}
				
				
				//TODO join with historic data
				
				
				/*
				 * construct
				 */
				if(constructTemplate != null && eventType != null){
					
					ConstructTemplateFiller ctf = new ConstructTemplateFiller(result.getVarBindings(), result.getDynamicArrays(), arrayTable);
					constructTemplate.accept(ctf);
					
					Model model = ctf.getModel();
						
					if(model != null){
		        		try {
		        				Iterator<Statement> it = model.iterator();
		        				while(it.hasNext()){
		        					Statement s = it.next();
		        						System.out.println("CoordinateListener ["+eventType+"]: "+s.getSubject().toString()+"   "+s.getPredicate().toString()+"   "+s.getObject().toString());
		        				}
		        				//System.out.println("CoordinateSystemListener [Construct event type]: "+eventType);
		        			runtime.sendEvent(new MapEvent<SesameEventModel>(new SesameEventModel(model)), eventType);
		        		} catch (EPException e) {
		        			e.printStackTrace();
		        		}
					}
				}
			}
			
		}
		
	}
}
