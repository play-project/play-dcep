/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Model;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.construct.ConstructTemplateFiller;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeSolutionSequence;
import eu.play_project.platformservices.querydispatcher.query.compiler.generation.util.RealTimeSolutionSequence.RealTimeSolution;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.ConstructTemplate;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLArrayFilter;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;


/**
 * @author ningyuan
 *
 */
public class RealTimeSolutionSequenceListener implements UpdateListener{
	
	private EPServiceProvider epService;
    
	private EPRuntime runtime;
    
	private final RealTimeSolutionSequence realTimeResults;
	
	private final List<BDPLArrayFilter> arrayFilters;
	
	private String eventType;
	
	private ConstructTemplate constructTemplate;
	
	/*
	 * mainly for static arrays
	 */
	private BDPLArrayTable arrayTable;
	
	public RealTimeSolutionSequenceListener(RealTimeSolutionSequence realTimeResults, List<BDPLArrayFilter> arrayFilters, ConstructTemplate constructTemplate, BDPLArrayTable arrayTable){
		this.realTimeResults = realTimeResults;
		this.arrayFilters = arrayFilters;
		this.constructTemplate = constructTemplate;
		this.arrayTable = arrayTable;
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
		
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		
		System.out.println(Thread.currentThread().getName()+"   RealTimeResultListener: ");
		
		
		
		for(int i = 0; i < newEvents.length; i++){
			System.out.println("RealTimeResultListener result "+i+": ");
			
			RealTimeSolution result = realTimeResults.get();
			
			if(result != null){
				for(Map<String, String[]> varBinding : result.getVarBindings()){
						
						System.out.println("RealTimeResultListener var binding:");
						for(String key : varBinding.keySet()){
							System.out.print(key+": "+varBinding.get(key)[0]+"   "+varBinding.get(key)[1]+"   ");
						}
						System.out.println();
						
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
				
				/*
				 * construct
				 */
				if(constructTemplate != null && eventType != null){
					ConstructTemplateFiller ctf = new ConstructTemplateFiller(result.getVarBindings(), result.getDynamicArrays(), arrayTable);
					constructTemplate.accept(ctf);
					
					Model model = ctf.getModel();
					
					if(model != null){
		        		try {
		        				
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
